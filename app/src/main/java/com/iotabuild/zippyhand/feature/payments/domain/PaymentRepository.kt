package com.iotabuild.zippyhand.feature.payments.domain

import com.iotabuild.zippyhand.core.model.ApiResponse

interface PaymentRepository {
    suspend fun createPaymentSession(request: PaymentSessionRequest): ApiResponse<PaymentSessionData>
    suspend fun verifyPayment(request: PaymentVerificationRequest): ApiResponse<CashfreeOrderResponse>
    suspend fun retryPayment(orderId: String): ApiResponse<RetryPaymentResponse>
}
