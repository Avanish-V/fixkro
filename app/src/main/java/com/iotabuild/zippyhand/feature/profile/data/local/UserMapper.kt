package com.iotabuild.zippyhand.feature.profile.data.local

import com.iotabuild.zippyhand.feature.profile.domain.UserResponse

fun UserResponse.toEntity(): UserEntity = UserEntity(
    id = 1,
    name = name,
    email = email,
    phoneNumber = phoneNumber
)

fun UserEntity.toDomain(): UserResponse = UserResponse(
    name = name,
    email = email,
    phoneNumber = phoneNumber
)
