package com.iotabuild.zippyhand.feature.onboarding.data

import android.util.Log
import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.feature.onboarding.domain.City
import com.iotabuild.zippyhand.feature.onboarding.domain.CityRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType

class CityRepositoryImpl(private val client: HttpClient) : CityRepository {
    override suspend fun getCities(): ApiResponse<List<City>> {
        return try {
            val response = client.get("cities") {
                contentType(ContentType.Application.Json)
            }
            response.body()
        } catch (e: Exception) {
            Log.e("CityRepository", "Error fetching cities", e)
            ApiResponse(success = false, data = emptyList())
        }
    }
}
