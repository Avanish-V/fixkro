package com.iotabuild.zippyhand.feature.address.data

import android.util.Log
import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.feature.address.domain.AddressResponse
import com.iotabuild.zippyhand.feature.address.domain.AddressRepository
import com.iotabuild.zippyhand.feature.address.domain.AddressRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AddressRepositoryImpl(private val client: HttpClient) : AddressRepository {

    override suspend fun getAddresses(): ApiResponse<List<AddressResponse>> {
        return try {
            val response = client.get("addresses") {
                contentType(ContentType.Application.Json)
            }
            Log.d("AddressRepository", "Response: ${response.body<ApiResponse<List<AddressResponse>>>()}")
            response.body()
        } catch (e: Exception) {
            Log.e("AddressRepository", "Error fetching addresses", e)
            ApiResponse(success = false, data = emptyList(), error = null)
        }
    }

    override suspend fun addAddress(address: AddressRequest): ApiResponse<AddressResponse> {
        return client.post("addresses") {
            contentType(ContentType.Application.Json)
            setBody(address)
        }.body()
    }

    override suspend fun deleteAddress(addressId: Long): ApiResponse<Unit> {
        return try {
            client.delete("addresses/$addressId") {
                contentType(ContentType.Application.Json)
            }.body()
        } catch (e: Exception) {
            Log.e("AddressRepository", "Error deleting address", e)
            ApiResponse(success = false, data = null, error = null)
        }
    }
}
