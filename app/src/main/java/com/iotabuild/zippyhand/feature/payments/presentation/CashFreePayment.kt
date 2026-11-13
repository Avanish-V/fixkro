package com.iotabuild.zippyhand.feature.payments.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashfree.pg.api.CFPaymentGatewayService
import com.cashfree.pg.base.exception.CFException
import com.cashfree.pg.core.api.CFSession
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback
import com.cashfree.pg.core.api.utils.CFErrorResponse
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment
import com.cashfree.pg.ui.api.upi.intent.CFUPIIntentCheckout
import com.cashfree.pg.ui.api.upi.intent.CFUPIIntentCheckoutPayment
import com.iotabuild.zippyhand.MainActivity
import com.iotabuild.zippyhand.feature.booking.domain.OrderRequest
import com.iotabuild.zippyhand.ui.theme.ZippyhandTheme
import com.iotabuild.zippyhand.utils.UiState
import org.koin.androidx.viewmodel.ext.android.viewModel

class CashFreePayment : ComponentActivity(), CFCheckoutResponseCallback {
    private var cfEnvironment = CFSession.Environment.PRODUCTION

    private val paymentViewModel : PaymentViewModel by viewModel()
    private var storedOrderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderRequest = intent.getParcelableExtra<OrderRequest>("order_request")

        // Handle deep-link return from Cashfree return_url: fixkro://payment/result?order_id=xxx
        // This fires when the SDK's internal WebView redirects to the deep-link after payment.
        val deepLinkOrderId = intent.data
            ?.takeIf { it.scheme == "fixkro" && it.host == "payment" }
            ?.getQueryParameter("order_id")

        if (deepLinkOrderId != null) {
            // Activity re-opened via deep-link — just verify status and show result UI
            storedOrderId = deepLinkOrderId
            setupContentView()
            paymentViewModel.verifyPayment(deepLinkOrderId)
            return
        }

        if (orderRequest == null) {
            finish()
            return
        }

        try {
            CFPaymentGatewayService.getInstance().setCheckoutCallback(this)
        } catch (e: CFException) {
            e.printStackTrace()
        }

        setupContentView(orderRequest)
    }

    // Called when Android re-opens this activity via the deep-link (launchMode=singleTop)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val deepLinkOrderId = intent.data
            ?.takeIf { it.scheme == "fixkro" && it.host == "payment" }
            ?.getQueryParameter("order_id")
        if (deepLinkOrderId != null) {
            storedOrderId = deepLinkOrderId
            paymentViewModel.verifyPayment(deepLinkOrderId)
        }
    }

    private fun navigateToBookings() {
        val intent = Intent(this@CashFreePayment, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAVIGATE_TO", "BOOKINGS")
        }
        startActivity(intent)
        finish()
    }

    private fun setupContentView(orderRequest: OrderRequest? = null) {
        setContent {
            ZippyhandTheme {
                val verification = paymentViewModel.paymentVerificationState
                val paymentSession = paymentViewModel.paymentSessionState
                val isActionInProgress = (verification is UiState.Loading || paymentSession is UiState.Loading)

                // Only kick off createOrder if we have an orderRequest (normal launch, not deep-link)
                if (orderRequest != null) {
                    LaunchedEffect(Unit) {
                        Log.d("ORDER_REQUEST", "$orderRequest")
                        paymentViewModel.createOrder(orderRequest)
                    }
                }

                LaunchedEffect(paymentSession) {
                    if (paymentSession is UiState.Success) {
                        val sessionData = paymentSession.data
                        Log.d("CashFreePayment", "Session Received: ID=${sessionData.orderId}, Token=${sessionData.paymentSessionId != null}")

                        storedOrderId = sessionData.orderId
                        Log.d("CashFreePayment", "Launching SDK for order: ${sessionData.orderId}")
                        doDropCheckoutPayment(sessionData.paymentSessionId, sessionData.orderId)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (verification != UiState.Idle) {
                        when (verification) {
                            is UiState.Loading -> PaymentLoadingCard("Verifying your payment...")
                            is UiState.Success -> {
                                val orderData = verification.data
                                val status = orderData?.order_status?.uppercase() ?: "UNKNOWN"

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    when {
                                        status == "PAID" || status == "SUCCESS" -> {
                                            PaymentSuccessCard(
                                                orderId = orderData?.order_id ?: "",
                                                amount = orderData?.order_amount ?: 0.0,
                                                transactionId = orderData?.cf_order_id,
                                                modifier = Modifier
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = { navigateToBookings() },
                                                enabled = !isActionInProgress,
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp)
                                                    .padding(horizontal = 16.dp)
                                            ) {
                                                Text("View Booking", fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        status == "PENDING" || status == "PROCESSING" -> {
                                            PaymentPendingCard(
                                                orderId = orderData?.order_id ?: "",
                                                amount = orderData?.order_amount ?: 0.0,
                                                modifier = Modifier
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        storedOrderId?.let { paymentViewModel.verifyPayment(it) }
                                                    },
                                                    enabled = !isActionInProgress,
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(50.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Refresh,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Check Status", fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = { navigateToBookings() },
                                                    enabled = !isActionInProgress,
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(50.dp)
                                                ) {
                                                    Text("View Booking", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        else -> { // FAILED, CANCELLED, USER_DROPPED, DECLINED, EXPIRED
                                            PaymentErrorCard(
                                                errorMessage = "Payment unsuccessful.",
                                                retryButtonText = "Try Again",
                                                enabled = !isActionInProgress,
                                                onRetry = {
                                                    storedOrderId?.let { paymentViewModel.retryPayment(it) }
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            OutlinedButton(
                                                onClick = { finish() },
                                                enabled = !isActionInProgress,
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp)
                                                    .padding(horizontal = 16.dp)
                                            ) {
                                                Text("Go Back", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            is UiState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    PaymentErrorCard(
                                        errorMessage = verification.message.ifBlank { "Payment unsuccessful." },
                                        retryButtonText = "Check Status",
                                        enabled = !isActionInProgress,
                                        onRetry = {
                                            storedOrderId?.let { paymentViewModel.verifyPayment(it) }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedButton(
                                        onClick = { finish() },
                                        enabled = !isActionInProgress,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text("Go Back", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            else -> {}
                        }
                    } else {
                        when (paymentSession) {
                            is UiState.Loading -> {
                                PaymentLoadingCard("Preparing payment...")
                            }
                            is UiState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    PaymentErrorCard(
                                        errorMessage = paymentSession.message,
                                        retryButtonText = "Try Again",
                                        enabled = !isActionInProgress,
                                        onRetry = {
                                            val orderId = storedOrderId
                                            if (orderId != null) {
                                                paymentViewModel.retryPayment(orderId)
                                            } else if (orderRequest != null) {
                                                paymentViewModel.createOrder(orderRequest)
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedButton(
                                        onClick = { finish() },
                                        enabled = !isActionInProgress,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text("Go Back", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            is UiState.Success -> {
                                PaymentLoadingCard("Launching payment gateway...")
                            }
                            else -> {
                                PaymentLoadingCard("Preparing...")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPaymentVerify(orderID: String?) {
        storedOrderId?.let { paymentViewModel.verifyPayment(it) }
    }

    override fun onPaymentFailure(cfErrorResponse: CFErrorResponse?, orderID: String?) {

        Log.e("CashFreePayment", "Order ID: $orderID")
        Log.e("CashFreePayment", "Error object: $cfErrorResponse")
        Log.e("CashFreePayment", "Error message: ${cfErrorResponse?.getMessage()}")
        Log.e("CashFreePayment", "Error code: ${cfErrorResponse?.getCode()}")

        // Verify with backend instead of trusting the SDK failure alone.
        // The SDK may report failure while a UPI/bank payment is still PENDING on the server.
        val resolvedOrderId = orderID ?: storedOrderId
        if (resolvedOrderId != null) {
            Log.d("CashFreePayment", "SDK reported failure for order $resolvedOrderId — verifying with backend")
            paymentViewModel.verifyPayment(resolvedOrderId)
        } else {
            val errorMessage = cfErrorResponse?.message ?: "Payment failed. Please try again."
            paymentViewModel.setVerificationError(errorMessage)
        }
    }

    private fun doDropCheckoutPayment(sessionId: String, orderID: String) {
        try {
            val cfSession = CFSession.CFSessionBuilder()
                .setEnvironment(cfEnvironment)
                .setPaymentSessionID(sessionId)
                .setOrderId(orderID)
                .build()

            Log.d("CashFreePayment", "SDK Session Built. Env: $cfEnvironment, OrderID: $orderID")

            val cfupiIntentCheckout = CFUPIIntentCheckout.CFUPIIntentBuilder()
                .setOrder(
                    listOf(
                        CFUPIIntentCheckout.CFUPIApps.BHIM,
                        CFUPIIntentCheckout.CFUPIApps.PHONEPE,
                        CFUPIIntentCheckout.CFUPIApps.GOOGLE_PAY,
                        CFUPIIntentCheckout.CFUPIApps.AMAZON_PAY,
                        CFUPIIntentCheckout.CFUPIApps.PAYTM
                    )
                )
                .build()

            val cfupiIntentCheckoutPayment = CFUPIIntentCheckoutPayment.CFUPIIntentPaymentBuilder()
                .setSession(cfSession)
                .setCfUPIIntentCheckout(cfupiIntentCheckout)
                .build()
            CFPaymentGatewayService.getInstance().doPayment(this, cfupiIntentCheckoutPayment)
        } catch (exception: CFException) {
            Log.d("CashFreePayment", "${exception.message}")
            exception.printStackTrace()
        }
    }

    private fun doWebCheckoutPayment(sessionId: String, orderID: String) {
        try {
            CFPaymentGatewayService.getInstance().setCheckoutCallback(this)
        } catch (e: CFException) {
            e.printStackTrace()
        }

        try {
            val cfSession = CFSession.CFSessionBuilder()
                .setEnvironment(cfEnvironment)
                .setPaymentSessionID(sessionId)
                .setOrderId(orderID)
                .build()

            val cfWebCheckoutPayment = CFWebCheckoutPayment.CFWebCheckoutPaymentBuilder()
                .setSession(cfSession)
                .build()
            CFPaymentGatewayService.getInstance().doPayment(this, cfWebCheckoutPayment)
        } catch (exception: CFException) {
            exception.printStackTrace()
        }
    }
}

@Composable
fun OrderStatus() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Successfully ordered!",
            style = MaterialTheme.typography.displayMedium
        )

        Text(
            text = "Order Id 1235689",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.displaySmall
        )
    }
}
