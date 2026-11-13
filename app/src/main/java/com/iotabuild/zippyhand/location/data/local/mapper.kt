package com.iotabuild.zippyhand.location.data.local

import com.iotabuild.zippyhand.location.domain.Location

fun LocationEntity.toDomain(): Location = Location(
    city = city,
    pinCode = pinCode,
    latitude = latitude,
    longitude = longitude,
    isServiceable = isServiceable,
    state = state,
    streetAddress = streetAddress
)