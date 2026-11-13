package com.iotabuild.zippyhand.location.domain

data class Location(
    val city: String,
    val pinCode: Long,
    val latitude: Double,
    val longitude: Double,
    val isServiceable: Boolean,
    val state: String = "",
    val streetAddress: String = ""
)
