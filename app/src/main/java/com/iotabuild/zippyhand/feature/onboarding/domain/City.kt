package com.iotabuild.zippyhand.feature.onboarding.domain

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val id: Long,
    val name: String,
    val symbol: String,
    val status: Boolean
)
