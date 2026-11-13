package com.iotabuild.zippyhand.feature.reviews.domain

import com.iotabuild.zippyhand.core.model.ApiResponse
import kotlinx.serialization.Serializable


interface ReviewRepository {
    suspend fun createReview(request: CreateReviewRequest): ApiResponse<ReviewDto>
    suspend fun getReviewsByProduct(productId: Long): Result<List<Review>>
    suspend fun getClientReviewsByProduct(productId: Long): ClientReviewsApiResponse
}
