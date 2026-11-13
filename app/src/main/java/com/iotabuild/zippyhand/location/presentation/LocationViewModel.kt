package com.iotabuild.zippyhand.location.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.location.domain.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationUiState(
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val hasLocationData: Boolean = false,
    val isServiceable: Boolean = false,
    val cityName: String = "Fetching your location...",
    val locationDetail: String = "Pinpointing accurate location on map...",
    val isFetchingLocation: Boolean = false,
    val hasPermission: Boolean = false,
    val error: String? = null
)

class LocationViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        observeSavedLocation()
    }

    private fun observeSavedLocation() {
        viewModelScope.launch {
            locationRepository.observeLocation().collect { location ->
                if (location != null) {
                    _uiState.update { current ->
                        current.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            hasLocationData = true,
                            cityName = location.city,
                            locationDetail = if (location.pinCode > 0) "Pincode: ${location.pinCode}" else "GPS Location pinpointed",
                            isFetchingLocation = false,
                            isServiceable = location.isServiceable
                        )
                    }
                }
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted) {
            fetchLocation()
        }
    }

    fun fetchLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLocation = true, error = null) }
            val result = locationRepository.fetchCurrentLocation()
            result.fold(
                onSuccess = { location ->
                    _uiState.update { current ->
                        current.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            hasLocationData = true,
                            cityName = location.city,
                            locationDetail = if (location.pinCode > 0) "Pincode: ${location.pinCode}" else "GPS Location pinpointed",
                            isFetchingLocation = false,
                            isServiceable = location.isServiceable
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isFetchingLocation = false,
                            error = throwable.message ?: "Failed to fetch location"
                        )
                    }
                }
            )
        }
    }
}
