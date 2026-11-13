package com.iotabuild.zippyhand.feature.address.domain

import kotlinx.serialization.Serializable

@Serializable
data class AddressResponse(
    val id: Long,
    val userId: String,
    val type: String,
    val fullName: String,
    val phone: String,
    val addressLine1: String,
    val city: String,
    val isDeleted: Boolean = false,
    val state: String,
    val postalCode: String,
    val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String? = null
)

@Serializable
data class AddressRequest(
    val type: String = "",
    val fullName: String,
    val phone: String,
    val addressLine1: String,
    val city: String,
    val isDeleted: Boolean = false,
    val state: String,
    val postalCode: String,
    val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String? = null
)
