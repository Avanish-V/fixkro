package com.iotabuild.zippyhand.feature.offers.domain

import kotlinx.serialization.Serializable

@Serializable
data class Offer(
    val offerCode: String,
    val discountType: String,
    val discountValue: Double,
    val categoryName: String? = null,
    val productName: String? = null,
    val shortDescription: String? = null,
    val status: String? = null
)

@Serializable
data class OffersResponse(
    val success: Boolean,
    val data: List<Offer>,
    val error: OfferError? = null
)

@Serializable
data class OfferError(
    val code: String,
    val message: String
)
