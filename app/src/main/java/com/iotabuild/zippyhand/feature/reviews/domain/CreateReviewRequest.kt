package com.iotabuild.zippyhand.feature.reviews.domain

import kotlinx.serialization.Serializable

@Serializable
data class CreateReviewRequest(
    val productId: Long,
    val orderId: Long,
    val customerName: String,
    val rating: Int,
    val technicianRating: Int,
    val technicianId: Long,
    val isRecommended: Boolean,
    val impressions: Set<String>,
    val comment: String
)
