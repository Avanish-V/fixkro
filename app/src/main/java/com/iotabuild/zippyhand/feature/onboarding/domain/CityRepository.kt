package com.iotabuild.zippyhand.feature.onboarding.domain

import com.iotabuild.zippyhand.core.model.ApiResponse

interface CityRepository {
    suspend fun getCities(): ApiResponse<List<City>>
}
