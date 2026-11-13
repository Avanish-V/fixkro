package com.iotabuild.zippyhand.feature.payments.domain

import kotlinx.serialization.Serializable

@Serializable
data class PaymentSessionRequest(
    val productId: Long,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String
)

@Serializable
data class PaymentSessionData(
    val orderId: String,
    val cfOrderId: String,
    val paymentSessionId: String,
    val orderAmount: Double,
    val orderCurrency: String,
    val customerDetails: CustomerDetails
)

@Serializable
data class CustomerDetails(
    val customerId: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String
)

@Serializable
data class PaymentVerificationRequest(
    val orderId: String
)

@Serializable
data class RetryPaymentRequest(
    val orderId: String
)

@Serializable
data class RetryPaymentResponse(
    val orderId: String,
    val cfOrderId: String,
    val paymentSessionId: String,
    val orderAmount: Double? = null,
    val currency: String = "INR"
)

@Serializable
data class PaymentVerificationResponse(
    val success: Boolean,
    val message: String,
    val orderStatus: String?,
    val data: CashfreeOrderResponse? = null
)

@Serializable
data class CashfreeOrderResponse(
    val order_id: String,
    val cf_order_id: String,
    val order_amount: Double,
    val order_status: String,
    val payment_session_id: String? = null
)
