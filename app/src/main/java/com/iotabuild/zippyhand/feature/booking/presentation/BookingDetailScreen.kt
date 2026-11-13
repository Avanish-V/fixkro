package com.iotabuild.zippyhand.feature.booking.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iotabuild.zippyhand.feature.booking.domain.MyOrderResponse
import com.iotabuild.zippyhand.ui.navigation.Route
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    orderId: Long,
    onBackClick: () -> Unit,
    onWriteReviewClick: (Route.WriteReview) -> Unit,
    viewModel: BookingDetailViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.fetchBookingDetail(orderId)
    }

    var showCancelDialog by remember { mutableStateOf(false) }
    
    if (showCancelDialog) {
        CancellationReasonSheet(
            onDismiss = { showCancelDialog = false },
            onConfirm = { reason ->
                viewModel.cancelBooking(orderId, reason)
                showCancelDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Booking Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is BookingDetailUiState.Loading -> {
                    BookingDetailShimmer()
                }
                is BookingDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Something went wrong",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchBookingDetail(orderId) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is BookingDetailUiState.Success -> {
                    BookingDetailContent(
                        order = state.order,
                        onWriteReviewClick = onWriteReviewClick,
                        onCancelClick = { showCancelDialog = true },
                        onRefreshClick = { viewModel.fetchBookingDetail(orderId) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingDetailContent(
    order: MyOrderResponse,
    onWriteReviewClick: (Route.WriteReview) -> Unit,
    onCancelClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    val isPaymentPending = order.paymentStatus.equals("PENDING", ignoreCase = true) ||
            order.status.equals("PENDING", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = order.orderId,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                order.scheduledAt?.let {
                    Text(
                        text = "Scheduled at: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            val (statusText, statusColor) = when {
                isPaymentPending -> "PAYMENT PENDING" to Color(0xFFFF9800)
                order.status == "COMPLETED" -> "COMPLETED" to Color(0xFF4CAF50)
                order.status == "CREATED" -> "CREATED" to Color(0xFF2196F3)
                order.status == "ASSIGNED" || order.status == "CONFIRMED" -> "CONFIRMED" to Color(0xFF00BCD4)
                order.status == "IN_PROGRESS" -> "IN PROGRESS" to Color(0xFF9C27B0)
                order.status == "CANCELLED" -> "CANCELLED" to Color(0xFFF44336)
                else -> order.status to Color.Gray
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OrderInfoCard(order = order)
                
                if (order.status == "COMPLETED") {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { 
                                onWriteReviewClick(
                                    Route.WriteReview(
                                        orderId = order.id,
                                        productId = order.productId
                                    )
                                )
                             },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Write Review", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else if (order.status != "COMPLETED" && order.status != "CANCELLED" && !isPaymentPending) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onCancelClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFEBEE),
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text("Cancel Booking", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (order.status == "CANCELLED") {
            Spacer(modifier = Modifier.height(24.dp))
            RefundStatusCard()
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Service Professional",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        ActiveProviderCard(
            name = order.technicianName ?: "Assigning Professional...",
            title = if (order.technicianId != null) "Certified Expert" else "Relax! We're assigning the best pro",
            imageUrl = order.technicianPhoto ?: "https://pifgeycbjxluaubdhmhh.supabase.co/storage/v1/object/public/BrandImages/boy.png"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Service Address",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                order.serviceAddress?.let { address ->
                    Text(
                        text = address.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${address.addressLine1}, ${address.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Phone: ${address.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F1F1))

                Text(
                    text = "Payment Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                BookingBillDetails(
                    itemTotal = BigDecimal(order.totalAmount),
                    discount = BigDecimal(order.discount ?: 0.0),
                    serviceTax = BigDecimal(order.tax),
                    basePrice = BigDecimal(order.price)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isPaymentPending) {
            PaymentProcessingCard(
                onRefreshClick = onRefreshClick
            )
        } else {
            Text(
                text = "Booking Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    BookingTimeline(
                        status = order.status,
                        createdAt = order.createdAt,
                        completedAt = order.completedAt
                    )
                }
            }
        }

        order.otp?.let { otp ->
            Spacer(modifier = Modifier.height(24.dp))
            OtpVerificationCard(otp = otp)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PaymentProcessingCard(
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E5E5))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = "Processing Payment",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFFFE082).copy(alpha = 0.5f))
            ) {
                Text(
                    text = "AWAITING CONFIRMATION",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "We are processing your payment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Please refresh or wait for 5 minutes. Your booking will update automatically once payment confirmation is received.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onRefreshClick,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Status",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Refresh Status",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RefundStatusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE8F5E9),
        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Refund Initiated",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Your refund is being processed and will reflect in your original payment method in 3-5 business days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF388E3C),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
