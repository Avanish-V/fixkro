package com.iotabuild.zippyhand.AppSatartUp

import com.iotabuild.zippyhand.feature.profile.domain.AuthRepository
import com.iotabuild.zippyhand.feature.profile.domain.AuthResult
import com.iotabuild.zippyhand.feature.profile.domain.StartUpState
import com.iotabuild.zippyhand.feature.profile.domain.UserResponse
import com.iotabuild.zippyhand.location.data.remote.ServiceAvailabilityResponse
import com.iotabuild.zippyhand.location.domain.Location
import com.iotabuild.zippyhand.location.domain.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAuthRepository : AuthRepository {
    private val userFlow = MutableStateFlow<UserResponse?>(null)
    var shouldThrowError = false

    fun setUser(user: UserResponse?) {
        userFlow.value = user
    }

    override fun getUserFromLocal(): Flow<UserResponse?> {
        if (shouldThrowError) {
            return flow { throw RuntimeException("Local DB Error") }
        }
        return userFlow
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult = AuthResult.IDLE
    override suspend fun checkUserExists(idToken: String): Boolean = false
    override suspend fun getUser(idToken: String): Result<UserResponse> = Result.failure(NotImplementedError())
    override suspend fun registerUser(idToken: String, name: String?, phoneNumber: String?): UserResponse = TODO()
    override suspend fun clearLocalUser() {}
}

class FakeLocationRepository : LocationRepository {
    private val locationFlow = MutableStateFlow<Location?>(null)
    var shouldThrowError = false

    fun setLocation(location: Location?) {
        locationFlow.value = location
    }

    override fun observeLocation(): Flow<Location?> {
        if (shouldThrowError) {
            return flow { throw RuntimeException("Location Error") }
        }
        return locationFlow
    }

    override suspend fun isExist(): Boolean = false
    override suspend fun fetchCurrentLocation(): Result<Location> = Result.failure(NotImplementedError())
    override suspend fun checkAvailability(pinCode: String): Result<ServiceAvailabilityResponse> = Result.failure(NotImplementedError())
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppStartUpViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeLocationRepository: FakeLocationRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeLocationRepository = FakeLocationRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun checkStartUp_whenUserIsNull_emitsLoginRequired() = runTest {
        fakeAuthRepository.setUser(null)

        val viewModel = AppStartUpViewModel(fakeAuthRepository, fakeLocationRepository)
        testScheduler.advanceUntilIdle()

        assertEquals(StartUpState.LoginRequired, viewModel.state.value)
    }

    @Test
    fun checkStartUp_whenUserExistsButLocationIsNull_emitsLocationGate() = runTest {
        fakeAuthRepository.setUser(UserResponse(name = "Test User", email = "test@example.com"))
        fakeLocationRepository.setLocation(null)

        val viewModel = AppStartUpViewModel(fakeAuthRepository, fakeLocationRepository)
        testScheduler.advanceUntilIdle()

        assertEquals(StartUpState.LocationGate, viewModel.state.value)
    }

    @Test
    fun checkStartUp_whenUserAndLocationExist_emitsHome() = runTest {
        val user = UserResponse(name = "Test User", email = "test@example.com")
        val location = Location(
            city = "New Delhi",
            pinCode = 110001,
            latitude = 28.6139,
            longitude = 77.2090,
            isServiceable = true
        )
        fakeAuthRepository.setUser(user)
        fakeLocationRepository.setLocation(location)

        val viewModel = AppStartUpViewModel(fakeAuthRepository, fakeLocationRepository)
        testScheduler.advanceUntilIdle()

        assertEquals(StartUpState.Home, viewModel.state.value)
    }

    @Test
    fun checkStartUp_flowUpdates_dynamicallyTransitionsStates() = runTest {
        fakeAuthRepository.setUser(null)
        fakeLocationRepository.setLocation(null)

        val viewModel = AppStartUpViewModel(fakeAuthRepository, fakeLocationRepository)

        val collectedStates = mutableListOf<StartUpState>()
        val job = launch {
            viewModel.state.collect { collectedStates.add(it) }
        }

        testScheduler.advanceUntilIdle()
        assertEquals(listOf(StartUpState.Loading, StartUpState.LoginRequired), collectedStates)

        // User logs in
        fakeAuthRepository.setUser(UserResponse(name = "Jane", email = "jane@example.com"))
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(StartUpState.Loading, StartUpState.LoginRequired, StartUpState.LocationGate), collectedStates)

        // Location is set
        val location = Location("Mumbai", 400001, 19.076, 72.877, true)
        fakeLocationRepository.setLocation(location)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(StartUpState.Loading, StartUpState.LoginRequired, StartUpState.LocationGate, StartUpState.Home), collectedStates)

        job.cancel()
    }

    @Test
    fun checkStartUp_whenErrorOccurs_emitsErrorState() = runTest {
        fakeAuthRepository.shouldThrowError = true

        val viewModel = AppStartUpViewModel(fakeAuthRepository, fakeLocationRepository)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is StartUpState.Error)
        assertEquals("Could not connect, please retry!", (viewModel.state.value as StartUpState.Error).message)
    }

    @Test
    fun checkStartUp_reInvoked_reRunsCheck() = runTest {
        fakeAuthRepository.setUser(null)
        val viewModel = AppStartUpViewModel(fakeAuthRepository, fakeLocationRepository)
        testScheduler.advanceUntilIdle()
        assertEquals(StartUpState.LoginRequired, viewModel.state.value)

        // Update repositories and re-trigger checkStartUp
        fakeAuthRepository.setUser(UserResponse(name = "Alex"))
        fakeLocationRepository.setLocation(Location("Delhi", 110001, 28.0, 77.0, true))
        viewModel.checkStartUp()
        testScheduler.advanceUntilIdle()

        assertEquals(StartUpState.Home, viewModel.state.value)
    }
}
