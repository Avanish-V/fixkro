package com.iotabuild.zippyhand.feature.profile.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null
)
