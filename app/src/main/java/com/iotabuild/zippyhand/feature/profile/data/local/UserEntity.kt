package com.iotabuild.zippyhand.feature.profile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null
)
