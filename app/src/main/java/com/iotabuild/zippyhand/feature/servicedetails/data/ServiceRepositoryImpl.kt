package com.iotabuild.zippyhand.feature.servicedetails.data

import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.core.network.parseApiResponse
import com.iotabuild.zippyhand.feature.booking.domain.OrderRequest
import com.iotabuild.zippyhand.feature.booking.domain.OrderResponse
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewDto
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewsApiResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.ProductResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.ProductsResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ServiceRepositoryImpl(private val client: HttpClient) : ServiceRepository {
    
    private val serviceCache = mutableMapOf<Int, Service>()
    private val servicesByCategoryCache = mutableMapOf<Int?, ProductsResponse>()
    private val reviewCache = mutableMapOf<Int, List<ReviewDto>>()

    override suspend fun getServices(categoryId: Int?): ProductsResponse {
        servicesByCategoryCache[categoryId]?.let { return it }

        val response: ProductsResponse = client.get("services") {
            if (categoryId != null) {
                parameter("categoryId", categoryId)
            }
        }.body()

        if (response.success) {
            servicesByCategoryCache[categoryId] = response
        }
        return response
    }

    override suspend fun getReviews(productId: Int): ReviewsApiResponse {
        reviewCache[productId]?.let {
            return ReviewsApiResponse(success = true, data = it, error = null)
        }

        val response: ReviewsApiResponse = client.get("reviews/product/$productId/client").body()
        if (response.success) {
            reviewCache[productId] = response.data
        }
        return response
    }

    override suspend fun getService(productId: Int): ProductResponse {
        serviceCache[productId]?.let {
            return ProductResponse(success = true, data = it, error = null)
        }
        
        val response: ProductResponse = client.get("product/$productId").body()
        if (response.success) {
            serviceCache[productId] = response.data
        }
        return response
    }

    override suspend fun createOrder(orderRequest: OrderRequest): ApiResponse<OrderResponse> {
        return client.post("order") {
            setBody(orderRequest)
            contentType(ContentType.Application.Json)
        }.parseApiResponse()
    }
}
