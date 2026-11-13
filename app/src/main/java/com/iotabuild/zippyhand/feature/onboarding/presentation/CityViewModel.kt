package com.iotabuild.zippyhand.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.onboarding.domain.City
import com.iotabuild.zippyhand.feature.onboarding.domain.CityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CityUiState {
    object Loading : CityUiState()
    data class Success(val cities: List<City>) : CityUiState()
    data class Error(val message: String) : CityUiState()
}

class CityViewModel(private val cityRepository: CityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<CityUiState>(CityUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchCities()
    }

    fun fetchCities() {
        viewModelScope.launch {
            _uiState.value = CityUiState.Loading
            val response = cityRepository.getCities()
            if (response.success && response.data != null) {
                _uiState.value = CityUiState.Success(response.data)
            } else {
                _uiState.value = CityUiState.Error(response.error?.message ?: "Failed to fetch cities")
            }
        }
    }
}
