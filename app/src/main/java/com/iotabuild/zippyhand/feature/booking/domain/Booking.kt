package com.iotabuild.zippyhand.feature.booking.domain

import kotlinx.serialization.Serializable

@Serializable
enum class BookingStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

@Serializable
data class Booking(
    val id: String,
    val serviceName: String,
    val serviceImageUrl: String,
    val providerName: String,
    val providerImageUrl: String,
    val date: String,
    val time: String,
    val address: String,
    val price: Double,
    val status: BookingStatus,
    val otp: String? = null
)
