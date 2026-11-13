package com.iotabuild.zippyhand.feature.offers.data

import android.util.Log
import com.iotabuild.zippyhand.feature.offers.domain.OfferRepository
import com.iotabuild.zippyhand.feature.offers.domain.OffersResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class OfferRepositoryImpl(private val httpClient: HttpClient) : OfferRepository {
    override suspend fun getOffers(): OffersResponse {
        return httpClient.get("offers").body()
    }

    override suspend fun getOffersByProduct(productId: Long): OffersResponse {
        val response = httpClient.get("offers/product/$productId")
        Log.d("OfferRepositoryImpl", "Response: $response")
        return response.body()
    }
}
