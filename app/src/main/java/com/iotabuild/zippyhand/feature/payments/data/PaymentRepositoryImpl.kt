package com.iotabuild.zippyhand.feature.payments.data

import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.core.network.parseApiResponse
import com.iotabuild.zippyhand.feature.payments.domain.CashfreeOrderResponse
import com.iotabuild.zippyhand.feature.payments.domain.PaymentRepository
import com.iotabuild.zippyhand.feature.payments.domain.PaymentSessionData
import com.iotabuild.zippyhand.feature.payments.domain.PaymentSessionRequest
import com.iotabuild.zippyhand.feature.payments.domain.PaymentVerificationRequest
import com.iotabuild.zippyhand.feature.payments.domain.RetryPaymentRequest
import com.iotabuild.zippyhand.feature.payments.domain.RetryPaymentResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PaymentRepositoryImpl(
    private val httpClient: HttpClient
) : PaymentRepository {

    override suspend fun createPaymentSession(
        request: PaymentSessionRequest
    ): ApiResponse<PaymentSessionData> {
        return httpClient.post("payment/create-session") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.parseApiResponse()
    }

    override suspend fun verifyPayment(
        request: PaymentVerificationRequest
    ): ApiResponse<CashfreeOrderResponse> {
        return httpClient.post("payment/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.parseApiResponse()
    }

    override suspend fun retryPayment(
        orderId: String
    ): ApiResponse<RetryPaymentResponse> {
        return httpClient.post("payment/retry") {
            contentType(ContentType.Application.Json)
            setBody(RetryPaymentRequest(orderId))
        }.parseApiResponse()
    }
}
