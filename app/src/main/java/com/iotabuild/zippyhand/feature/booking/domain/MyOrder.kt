package com.iotabuild.zippyhand.feature.booking.domain

import com.iotabuild.zippyhand.feature.address.domain.AddressRequest
import com.iotabuild.zippyhand.feature.address.domain.AddressResponse
import kotlinx.serialization.Serializable

@Serializable
data class MyOrderResponse(
    val id: Long,
    val orderId: String,
    val totalAmount: Double,
    val status: String,
    val paymentStatus: String,
    val createdAt: String,
    val completedAt: String? = null,
    val scheduledAt: String? = null,
    val otp: String? = null,
    val serviceAddress: OrderAddress? = null,
    val productId: Long,
    val productTitle: String,
    val productImage: String,
    val price: Double,
    val discount: Double? = null,
    val tax: Double,
    val quantity: Int = 0,
    val technicianId: Long? = null,
    val technicianName: String? = null,
    val technicianPhoto: String? = null
)

@Serializable
data class MyOrdersData(
    val content: List<MyOrderResponse>,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int
)

@Serializable
data class MyOrdersApiResponse(
    val success: Boolean,
    val data: MyOrdersData,
    val error: OrderError? = null
)

@Serializable
data class SingleOrderApiResponse(
    val success: Boolean,
    val data: MyOrderResponse,
    val error: OrderError? = null
)

@Serializable
data class OrderError(
    val code: String,
    val message: String
)

@Serializable
data class OrderAddress(
    val fullName: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val state: String,
    val postalCode: String,
)
