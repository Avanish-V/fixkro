package com.iotabuild.zippyhand.feature.reviews.domain

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    val userName: String,
    val userImage: String,
    val rating: Double,
    val date: String,
    val comment: String
)
