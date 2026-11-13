package com.iotabuild.zippyhand.feature.reviews.domain

import com.iotabuild.zippyhand.core.model.ApiError
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val id: Long,
    val productId: Long,
    val orderId: Long,
    val customerName: String,
    val rating: Int,
    val technicianRating: Int,
    val technicianId: Long,
    val isRecommended: Boolean,
    val impressions: Set<String>,
    val comment: String,
    val createdAt: String
)

@Serializable
data class ReviewsApiResponse(
    val success: Boolean,
    val data: List<ReviewDto>,
    val error: ApiError? = null
)

/**
 * Response wrapper for the mobile-friendly review list.
 * Maps to GET /api/v1/reviews/product/{productId}/client
 */
@Serializable
data class ClientReviewsApiResponse(
    val success: Boolean,
    val data: List<Review>,
    val error: ApiError? = null
)
