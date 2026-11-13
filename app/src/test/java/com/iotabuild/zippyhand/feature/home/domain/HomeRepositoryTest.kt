package com.iotabuild.zippyhand.feature.home.domain

import com.iotabuild.zippyhand.feature.home.data.remote.HomeResponse
import com.iotabuild.zippyhand.feature.servicecategory.domain.Category
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeHomeRepository : HomeRepository {
    var shouldReturnError = false
    var homeResponse = HomeResponse(
        banner = "https://example.com/banner.png",
        categories = listOf(Category(categoryId = 1, title = "AC Service", image = "ac_icon.png")),
        services = emptyList()
    )

    override suspend fun getHomeData(forceRefresh: Boolean): Result<HomeResponse> {
        return if (shouldReturnError) {
            Result.failure(Exception("Failed to fetch home data"))
        } else {
            Result.success(homeResponse)
        }
    }

    override suspend fun getServicesForCategory(
        categoryId: Int,
        forceRefresh: Boolean
    ): Result<List<Service>> {
        return if (shouldReturnError) {
            Result.failure(Exception("Failed to fetch services"))
        } else {
            Result.success(emptyList())
        }
    }

    override fun isCategoryCached(categoryId: Int): Boolean = true

    override fun clearCache() {}
}

class HomeRepositoryTest {

    @Test
    fun getHomeData_success_returnsHomeResponse() = runBlocking {
        val repo = FakeHomeRepository()
        val result = repo.getHomeData()

        assertTrue(result.isSuccess)
        assertEquals("https://example.com/banner.png", result.getOrNull()?.banner)
        assertEquals(1, result.getOrNull()?.categories?.size)
    }

    @Test
    fun getHomeData_failure_returnsFailure() = runBlocking {
        val repo = FakeHomeRepository()
        repo.shouldReturnError = true

        val result = repo.getHomeData()

        assertTrue(result.isFailure)
        assertEquals("Failed to fetch home data", result.exceptionOrNull()?.message)
    }
}
