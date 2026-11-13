package com.iotabuild.zippyhand.location.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val city: String,
    val pinCode: Long,
    val latitude: Double,
    val longitude: Double,
    val isServiceable: Boolean,
    val state: String = "",
    val streetAddress: String = ""
)
