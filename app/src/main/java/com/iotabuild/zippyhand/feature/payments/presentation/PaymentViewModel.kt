package com.iotabuild.zippyhand.feature.payments.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.booking.domain.OrderRequest
import com.iotabuild.zippyhand.feature.booking.domain.OrderResponse
import com.iotabuild.zippyhand.feature.payments.domain.CashfreeOrderResponse
import com.iotabuild.zippyhand.feature.payments.domain.PaymentRepository
import com.iotabuild.zippyhand.feature.payments.domain.PaymentSessionRequest
import com.iotabuild.zippyhand.feature.payments.domain.PaymentVerificationRequest
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceRepository
import com.iotabuild.zippyhand.utils.UiState
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    var paymentSessionState by mutableStateOf<UiState<OrderResponse>>(UiState.Idle)
        private set

    var paymentVerificationState by mutableStateOf<UiState<CashfreeOrderResponse?>>(UiState.Idle)
        private set

    var currentOrderId by mutableStateOf<String?>(null)
        private set

    fun createOrder(orderRequest: OrderRequest) {
        viewModelScope.launch {
            paymentSessionState = UiState.Loading
            try {
                val result = serviceRepository.createOrder(orderRequest)
                if (result.success && result.data != null) {
                    paymentSessionState = UiState.Success(data = result.data)
                    currentOrderId = result.data.orderId
                    Log.d("PaymentViewModel", "Order created successfully. CF Order ID: ${result.data}")
                } else {
                    paymentSessionState = UiState.Error(result.error?.message ?: "Failed to create order")
                }
            } catch (e: Exception) {
                paymentSessionState = UiState.Error(e.message ?: "Network error during order creation")
            }
        }
    }

    fun createPaymentSession(
        productId: Long = 0,
        customerName: String,
        customerEmail: String,
        customerPhone: String
    ) {
        viewModelScope.launch {
            paymentSessionState = UiState.Loading
            val request = PaymentSessionRequest(productId, customerName, customerEmail, customerPhone)
            val result = paymentRepository.createPaymentSession(request)

            if (result.success && result.data != null) {
                val mappedResponse = OrderResponse(
                    orderId = result.data.orderId,
                    cfOrderId = result.data.cfOrderId,
                    paymentSessionId = result.data.paymentSessionId
                )
                paymentSessionState = UiState.Success(data = mappedResponse)
                currentOrderId = result.data.cfOrderId
            } else {
                paymentSessionState = UiState.Error(result.error?.message ?: "Payment session failed")
            }
        }
    }

    fun verifyPayment(orderId: String = currentOrderId ?: "") {
        if (orderId.isEmpty()) {
            paymentVerificationState = UiState.Error("Order ID not found")
            return
        }

        viewModelScope.launch {
            paymentVerificationState = UiState.Loading

            val request = paymentRepository.verifyPayment(PaymentVerificationRequest(orderId))
            if (request.success && request.data != null) {
                paymentVerificationState = UiState.Success(data = request.data)
            } else {
                paymentVerificationState = UiState.Error(request.error?.message ?: "Failed to verify payment")
            }
        }
    }

    fun retryPayment(orderId: String = currentOrderId ?: "") {
        if (orderId.isEmpty()) {
            paymentVerificationState = UiState.Error("Order ID not found")
            return
        }

        viewModelScope.launch {
            paymentVerificationState = UiState.Loading
            try {
                val response = paymentRepository.retryPayment(orderId)
                if (response.success && response.data != null) {
                    val sessionData = response.data
                    currentOrderId = sessionData.orderId
                    paymentSessionState = UiState.Success(
                        data = OrderResponse(
                            orderId = sessionData.orderId,
                            cfOrderId = sessionData.cfOrderId,
                            paymentSessionId = sessionData.paymentSessionId
                        )
                    )
                    paymentVerificationState = UiState.Idle
                } else {
                    val errorMsg = response.error?.message ?: "Failed to retry payment"
                    paymentVerificationState = UiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                paymentVerificationState = UiState.Error(e.message ?: "Network error during retry")
            }
        }
    }

    fun resetPaymentSession() {
        paymentSessionState = UiState.Idle
        paymentVerificationState = UiState.Idle
        currentOrderId = null
    }

    fun resetVerification() {
        paymentVerificationState = UiState.Idle
    }

    fun setVerificationError(message: String) {
        paymentVerificationState = UiState.Error(message)
    }
}
