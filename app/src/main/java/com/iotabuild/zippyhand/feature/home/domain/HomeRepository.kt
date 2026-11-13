package com.iotabuild.zippyhand.feature.home.domain

import com.iotabuild.zippyhand.feature.home.data.remote.HomeResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service

interface HomeRepository {
    suspend fun getHomeData(forceRefresh: Boolean = false): Result<HomeResponse>
    suspend fun getServicesForCategory(categoryId: Int, forceRefresh: Boolean = false): Result<List<Service>>
    fun isCategoryCached(categoryId: Int): Boolean
    fun clearCache()
}
