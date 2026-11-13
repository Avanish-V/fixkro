package com.iotabuild.zippyhand.feature.servicedetails.domain

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val productId: Int,
    val categoryId: Int,
    val title: String,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val price: Long = 0L,
    val originalPrice: Double = 0.0,
    val providerName: String = "Zippyhand",
    val providerImageUrl: String = "",
    val imageUrl: String = "",
    val serviceType: String = "",
    val status: Boolean = true,
    val categoryImage: String? = null,
    val descriptions: List<ServiceDescriptionDto> = emptyList()
) {
    val id: String
        get() = productId.toString()
}

@Serializable
data class ServiceDescriptionDto(
    val id: Long? = null,
    val title: String,
    val shortDescription: String
)

@Serializable
data class ProductsResponse(
    val success: Boolean,
    val data: List<Service>,
    val error: ServiceError? = null
)

@Serializable
data class ProductResponse(
    val success: Boolean,
    val data: Service,
    val error: ServiceError? = null
)

@Serializable
data class ServiceError(
    val code: String,
    val message: String
)

data class DescriptionStep(
    val title: String,
    val description: String
)
