package com.iotabuild.zippyhand.feature.address.domain

import com.iotabuild.zippyhand.core.model.ApiResponse

interface AddressRepository {
    suspend fun getAddresses(): ApiResponse<List<AddressResponse>>
    suspend fun addAddress(address: AddressRequest): ApiResponse<AddressResponse>
    suspend fun deleteAddress(addressId: Long): ApiResponse<Unit>
}
