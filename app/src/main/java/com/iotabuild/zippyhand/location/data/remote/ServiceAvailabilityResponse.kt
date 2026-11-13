package com.iotabuild.zippyhand.location.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ServiceAvailabilityResponse(
    val isServiceable: Boolean,
    val pincode: String,
    val cityName: String?,
    val state: String?,
    val localities: List<String>,
    val deliveryNote: String?,
    val message: String
)
