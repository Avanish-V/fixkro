package com.iotabuild.zippyhand.feature.home.data.remote

import android.util.Log
import com.iotabuild.zippyhand.feature.home.data.local.HomeDao
import com.iotabuild.zippyhand.feature.home.domain.HomeRepository
import com.iotabuild.zippyhand.feature.servicedetails.domain.ProductsResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class HomeRepoImpl(
    private val httpClient: HttpClient,
    private val homeDao: HomeDao
) : HomeRepository {

    private var cachedHomeResponse: HomeResponse? = null
    private val servicesByCategoryCache = mutableMapOf<Int, List<Service>>()

    override suspend fun getHomeData(forceRefresh: Boolean): Result<HomeResponse> {
        if (!forceRefresh && cachedHomeResponse != null) {
            Log.d("HomeRepoImpl", "Serving Home Data from Memory Cache")
            return Result.success(cachedHomeResponse!!)
        }

        return try {
            val response = httpClient.get("home")

            if (response.status.value in 200..299) {
                val homeResponse = response.body<HomeResponse>()
                cachedHomeResponse = homeResponse

                if (homeResponse.services.isNotEmpty()) {
                    val groupedServices = homeResponse.services.groupBy { it.categoryId }
                    groupedServices.forEach { (catId, serviceList) ->
                        servicesByCategoryCache[catId] = serviceList
                    }
                }

                val firstCategory = homeResponse.categories.firstOrNull()
                if (firstCategory != null && !servicesByCategoryCache.containsKey(firstCategory.categoryId) && homeResponse.services.isNotEmpty()) {
                    servicesByCategoryCache[firstCategory.categoryId] = homeResponse.services
                }

                Log.d("HomeRepoImpl", "Fetched home data from API & cached: $homeResponse")
                Result.success(homeResponse)
            } else {
                Log.e("HomeRepoImpl", "Failed to fetch home data HTTP status: ${response.status.value}")
                Result.failure(Exception("Failed to fetch home data (${response.status.value})"))
            }
        } catch (e: Exception) {
            Log.e("HomeRepoImpl", "Exception in getHomeData", e)
            if (cachedHomeResponse != null) {
                Log.d("HomeRepoImpl", "Network error, falling back to cached Home Data")
                Result.success(cachedHomeResponse!!)
            } else {
                Result.failure(Exception("Could not connect to server, please retry!"))
            }
        }
    }

    override suspend fun getServicesForCategory(
        categoryId: Int,
        forceRefresh: Boolean
    ): Result<List<Service>> {
        if (!forceRefresh && servicesByCategoryCache.containsKey(categoryId)) {
            val cachedList = servicesByCategoryCache[categoryId] ?: emptyList()
            Log.d("HomeRepoImpl", "Serving services for category $categoryId from Memory Cache (size=${cachedList.size})")
            return Result.success(cachedList)
        }

        return try {
            val response = httpClient.get("services") {
                parameter("categoryId", categoryId)
            }

            if (response.status.value in 200..299) {
                val productsResponse = response.body<ProductsResponse>()
                if (productsResponse.success) {
                    servicesByCategoryCache[categoryId] = productsResponse.data
                    Log.d("HomeRepoImpl", "Fetched & cached ${productsResponse.data.size} services for category $categoryId")
                    Result.success(productsResponse.data)
                } else {
                    Result.failure(Exception(productsResponse.error?.message ?: "Failed to fetch services"))
                }
            } else {
                Result.failure(Exception("Server returned status ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("HomeRepoImpl", "Exception in getServicesForCategory($categoryId)", e)
            if (servicesByCategoryCache.containsKey(categoryId)) {
                Result.success(servicesByCategoryCache[categoryId] ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load services for category"))
            }
        }
    }

    override fun isCategoryCached(categoryId: Int): Boolean {
        return servicesByCategoryCache.containsKey(categoryId)
    }

    override fun clearCache() {
        cachedHomeResponse = null
        servicesByCategoryCache.clear()
    }
}
