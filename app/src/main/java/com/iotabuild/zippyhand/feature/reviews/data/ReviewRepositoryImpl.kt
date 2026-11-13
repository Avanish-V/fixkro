package com.iotabuild.zippyhand.feature.reviews.data

import com.iotabuild.zippyhand.core.model.ApiError
import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.feature.reviews.domain.ClientReviewsApiResponse
import com.iotabuild.zippyhand.feature.reviews.domain.CreateReviewRequest
import com.iotabuild.zippyhand.feature.reviews.domain.Review
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewDto
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewRepository
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewsApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ReviewRepositoryImpl(private val client: HttpClient) : ReviewRepository {

    override suspend fun createReview(request: CreateReviewRequest): ApiResponse<ReviewDto> {
        return try {
            client.post("reviews") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }.body()
        } catch (e: Exception) {
            ApiResponse(false, null, ApiError(e.message ?: "Unknown Error", null))
        }
    }

    override suspend fun getReviewsByProduct(productId: Long): Result<List<Review>> {
        return try {
            val response = client.get("client/reviews/product/$productId")
            if(response.status.value in 200..299){
                Result.success(response.body())
            }else{
                Result.failure(Exception(response.body<ReviewsApiResponse>().error?.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getClientReviewsByProduct(productId: Long): ClientReviewsApiResponse {
        return try {
            client.get("reviews/product/$productId/client").body()
        } catch (e: Exception) {
            ClientReviewsApiResponse(false, emptyList(), null)
        }
    }
}
