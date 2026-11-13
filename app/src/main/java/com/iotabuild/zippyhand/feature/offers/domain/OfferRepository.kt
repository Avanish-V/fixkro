package com.iotabuild.zippyhand.feature.offers.domain

interface OfferRepository {
    suspend fun getOffers(): OffersResponse
    suspend fun getOffersByProduct(productId: Long): OffersResponse
}
