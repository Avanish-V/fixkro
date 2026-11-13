package com.iotabuild.zippyhand.location.domain

import com.iotabuild.zippyhand.location.data.remote.ServiceAvailabilityResponse
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    suspend fun isExist() : Boolean
    fun observeLocation(): Flow<Location?>
    suspend fun fetchCurrentLocation(): Result<Location>

    suspend fun checkAvailability(pinCode: String): Result<ServiceAvailabilityResponse>
}
