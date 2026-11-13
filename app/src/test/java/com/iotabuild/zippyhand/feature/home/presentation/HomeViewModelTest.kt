package com.iotabuild.zippyhand.feature.home.presentation

import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.feature.booking.domain.OrderRequest
import com.iotabuild.zippyhand.feature.booking.domain.OrderResponse
import com.iotabuild.zippyhand.feature.home.data.remote.HomeResponse
import com.iotabuild.zippyhand.feature.home.domain.HomeRepository
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewsApiResponse
import com.iotabuild.zippyhand.feature.servicecategory.domain.Category
import com.iotabuild.zippyhand.feature.servicedetails.domain.ProductResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.ProductsResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceError
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceRepository
import com.iotabuild.zippyhand.location.data.remote.ServiceAvailabilityResponse
import com.iotabuild.zippyhand.location.domain.Location
import com.iotabuild.zippyhand.location.domain.LocationRepository
import com.iotabuild.zippyhand.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeServiceRepository : ServiceRepository {
    var shouldReturnError = false
    var services = listOf(
        Service(
            productId = 101,
            categoryId = 1,
            title = "AC Foam Jet Service",
            rating = 4.8,
            reviewCount = 120,
            price = 599,
            originalPrice = 899.0,
            providerName = "Zippyhand Tech",
            providerImageUrl = "",
            imageUrl = "",
            serviceType = "Deep Service",
            status = true
        )
    )

    override suspend fun getServices(categoryId: Int?): ProductsResponse {
        return if (shouldReturnError) {
            ProductsResponse(success = false, data = emptyList(), error = ServiceError("500", "Failed to fetch"))
        } else {
            ProductsResponse(success = true, data = services, error = null)
        }
    }

    override suspend fun getService(productId: Int): ProductResponse {
        return ProductResponse(success = true, data = services.first(), error = null)
    }

    override suspend fun getReviews(productId: Int): ReviewsApiResponse {
        return ReviewsApiResponse(success = true, data = emptyList(), error = null)
    }

    override suspend fun createOrder(orderRequest: OrderRequest): ApiResponse<OrderResponse> {
        throw NotImplementedError()
    }
}

class FakeHomeRepository : HomeRepository {
    var shouldReturnError = false
    var sampleServices = listOf(
        Service(
            productId = 101,
            categoryId = 1,
            title = "AC Foam Jet Service",
            rating = 4.8,
            reviewCount = 120,
            price = 599,
            originalPrice = 899.0,
            providerName = "Zippyhand Tech",
            providerImageUrl = "",
            imageUrl = "",
            serviceType = "Deep Service",
            status = true
        )
    )
    var homeResponse = HomeResponse(
        banner = "https://example.com/banner.png",
        categories = listOf(
            Category(categoryId = 1, title = "AC Service", image = "ac_icon.png"),
            Category(categoryId = 2, title = "Cleaning", image = "cleaning_icon.png")
        ),
        services = emptyList()
    )

    override suspend fun getHomeData(forceRefresh: Boolean): Result<HomeResponse> {
        return if (shouldReturnError) {
            Result.failure(Exception("Server Error"))
        } else {
            Result.success(homeResponse)
        }
    }

    override suspend fun getServicesForCategory(
        categoryId: Int,
        forceRefresh: Boolean
    ): Result<List<Service>> {
        return if (shouldReturnError) {
            Result.failure(Exception("Server Error"))
        } else {
            Result.success(sampleServices)
        }
    }

    override fun isCategoryCached(categoryId: Int): Boolean = true

    override fun clearCache() {}
}

class FakeLocationRepo : LocationRepository {
    private val locationFlow = MutableStateFlow<Location?>(null)

    override fun observeLocation(): Flow<Location?> = locationFlow
    override suspend fun isExist(): Boolean = false
    override suspend fun fetchCurrentLocation(): Result<Location> = Result.failure(NotImplementedError())
    override suspend fun checkAvailability(pinCode: String): Result<ServiceAvailabilityResponse> = Result.failure(NotImplementedError())
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeServiceRepo: FakeServiceRepository
    private lateinit var fakeHomeRepo: FakeHomeRepository
    private lateinit var fakeLocationRepo: FakeLocationRepo

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeServiceRepo = FakeServiceRepository()
        fakeHomeRepo = FakeHomeRepository()
        fakeLocationRepo = FakeLocationRepo()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadHomeData_success_updatesHomeStateToSuccess() = runTest {
        val viewModel = HomeViewModel(fakeServiceRepo, fakeHomeRepo, fakeLocationRepo)
        testScheduler.advanceUntilIdle()

        val state = viewModel.homeState.value
        assertTrue(state is UiState.Success)
        val homeData = (state as UiState.Success).data
        assertEquals("https://example.com/banner.png", homeData.banner)
        assertEquals(2, homeData.categories.size)
        assertEquals(1, viewModel.selectedCategory.value?.categoryId)
    }

    @Test
    fun loadHomeData_failure_updatesHomeStateToError() = runTest {
        fakeHomeRepo.shouldReturnError = true
        val viewModel = HomeViewModel(fakeServiceRepo, fakeHomeRepo, fakeLocationRepo)
        testScheduler.advanceUntilIdle()

        val state = viewModel.homeState.value
        assertTrue(state is UiState.Error)
        assertEquals("Server Error", (state as UiState.Error).message)
    }

    @Test
    fun onCategorySelected_success_updatesServices() = runTest {
        val viewModel = HomeViewModel(fakeServiceRepo, fakeHomeRepo, fakeLocationRepo)
        testScheduler.advanceUntilIdle()

        val category = Category(categoryId = 1, title = "AC Service", image = "ac_icon.png")
        viewModel.onCategorySelected(category)
        testScheduler.advanceUntilIdle()

        val state = viewModel.homeState.value
        assertTrue(state is UiState.Success)
        val homeData = (state as UiState.Success).data
        assertEquals(1, homeData.services.size)
        assertEquals("AC Foam Jet Service", homeData.services.first().title)
    }
}
