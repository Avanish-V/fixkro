package com.iotabuild.zippyhand.feature.address.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.address.domain.AddressResponse
import com.iotabuild.zippyhand.feature.address.domain.AddressRepository
import com.iotabuild.zippyhand.feature.address.domain.AddressRequest
import com.iotabuild.zippyhand.location.domain.Location
import com.iotabuild.zippyhand.location.domain.LocationRepository
import com.iotabuild.zippyhand.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddressViewModel(
    private val repository: AddressRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _addressState = MutableStateFlow<UiState<List<AddressResponse>>>(UiState.Loading)
    val addressState: StateFlow<UiState<List<AddressResponse>>> = _addressState.asStateFlow()

    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation: StateFlow<Boolean> = _isFetchingLocation.asStateFlow()

    init {
        fetchAddresses()
    }

    fun fetchAddresses() {
        viewModelScope.launch {
            _addressState.value = UiState.Loading
            try {
                val response = repository.getAddresses()
                if (response.success) {
                    Log.d("AddressViewModel", "Fetched addresses: ${response.data}")
                    _addressState.value = UiState.Success(response.data ?: emptyList())
                } else {
                    _addressState.value = UiState.Error(response.error?.message ?: "Failed to fetch addresses")
                }
            } catch (e: Exception) {
                _addressState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun fetchCurrentLocation(
        onSuccess: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isFetchingLocation.value = true
            val result = locationRepository.fetchCurrentLocation()
            _isFetchingLocation.value = false
            result.fold(
                onSuccess = { location ->
                    onSuccess(location)
                },
                onFailure = { throwable ->
                    onError(throwable.message ?: "Failed to fetch current location")
                }
            )
        }
    }

    fun addAddress(address: AddressRequest) {
        viewModelScope.launch {
            val result = repository.addAddress(address)
            if (result.success) {
                val newAddress = result.data
                val currentState = _addressState.value
                if (newAddress != null && currentState is UiState.Success) {
                    val updatedList = listOf(newAddress) + currentState.data
                    _addressState.value = UiState.Success(updatedList)
                } else {
                    fetchAddresses()
                }
            }
        }
    }

    fun deleteAddress(addressId: Long) {
        viewModelScope.launch {
            val currentState = _addressState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.filter { it.id?.toLong() != addressId }
                _addressState.value = UiState.Success(updatedList)
            }

            val result = repository.deleteAddress(addressId)
            if (!result.success) {
                fetchAddresses()
            }
        }
    }
}
