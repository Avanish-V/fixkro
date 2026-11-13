package com.iotabuild.zippyhand.feature.booking.domain

import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val orderId: String,
    val cfOrderId: String,
    val paymentSessionId: String
)
