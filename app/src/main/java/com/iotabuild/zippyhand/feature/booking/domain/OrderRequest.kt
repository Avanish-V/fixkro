package com.iotabuild.zippyhand.feature.booking.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class OrderRequest(
    val addressId: Long,
    val productId: Long,
    val quantity: Int = 1,
    val offerCode: String? = null,
    val createdAt: String,
    val serviceSchedule: String
) : Parcelable
