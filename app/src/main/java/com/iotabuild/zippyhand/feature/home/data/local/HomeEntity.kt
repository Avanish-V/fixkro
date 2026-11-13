package com.iotabuild.zippyhand.feature.home.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home")
data class HomeEntity(
    @PrimaryKey val id: Int = 1,
    val banner: String,
    val category: String
)
