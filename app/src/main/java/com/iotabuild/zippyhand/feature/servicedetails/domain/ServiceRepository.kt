package com.iotabuild.zippyhand.feature.servicedetails.domain

import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.feature.booking.domain.OrderRequest
import com.iotabuild.zippyhand.feature.booking.domain.OrderResponse
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewsApiResponse

interface ServiceRepository {
    suspend fun getServices(categoryId: Int? = null): ProductsResponse
    suspend fun getService(productId: Int): ProductResponse
    suspend fun getReviews(productId: Int): ReviewsApiResponse
    suspend fun createOrder(orderRequest: OrderRequest): ApiResponse<OrderResponse>
}
