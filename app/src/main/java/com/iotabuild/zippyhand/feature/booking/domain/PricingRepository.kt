package com.iotabuild.zippyhand.feature.booking.domain

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

interface PricingInterface {
    suspend fun getPricing(pricingRequest: PricingRequest): PricingResponse
}

class PricingRepository(private val httpClient: HttpClient) : PricingInterface {
    override suspend fun getPricing(pricingRequest: PricingRequest): PricingResponse {
        return httpClient.post("pricing/calculate") {
            contentType(ContentType.Application.Json)
            setBody(pricingRequest)
        }.body()
    }
}

@Serializable
data class PricingRequest(
    val productId: Long,
    val quantity: Int,
    val offerCode: String?
)
