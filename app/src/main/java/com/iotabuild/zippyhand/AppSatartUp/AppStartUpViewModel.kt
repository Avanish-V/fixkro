package com.iotabuild.zippyhand.AppSatartUp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iotabuild.zippyhand.feature.profile.domain.AuthRepository
import com.iotabuild.zippyhand.feature.profile.domain.StartUpState
import com.iotabuild.zippyhand.location.domain.LocationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class AppStartUpViewModel(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
): ViewModel() {

    private val _state = MutableStateFlow<StartUpState>(StartUpState.Loading)
    val state : StateFlow<StartUpState> = _state.asStateFlow()

    private var checkStartUpJob: Job? = null

    init {
        checkStartUp()
    }

    fun checkStartUp(){
        checkStartUpJob?.cancel()
        checkStartUpJob = viewModelScope.launch {
            try {
                authRepository.getUserFromLocal()
                    .flatMapLatest { user ->
                        if (user != null) {
                            locationRepository.observeLocation().map { location ->
                                if (location != null) {
                                    StartUpState.Home
                                } else {
                                    StartUpState.LocationGate
                                }
                            }

                        } else {
                            flowOf(StartUpState.LoginRequired)
                        }
                    }
                    .collect { newState ->
                        _state.value = newState
                    }



            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("AppStartUpViewModel", "checkStartUp: ${e.message}")
                _state.value = StartUpState.Error("Could not connect, please retry!")
            }
        }
    }

}