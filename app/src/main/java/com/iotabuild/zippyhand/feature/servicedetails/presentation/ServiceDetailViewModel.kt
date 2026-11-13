package com.iotabuild.zippyhand.feature.servicedetails.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.reviews.domain.Review
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceDetailsPage
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceRepository
import com.iotabuild.zippyhand.feature.servicedetails.usecase.GetServiceDetailsUseCase
import com.iotabuild.zippyhand.utils.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServiceDetailViewModel(
    private val getServicesUseCase: GetServiceDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ServiceDetailsPage>>(UiState.Loading)
    val uiState : StateFlow<UiState<ServiceDetailsPage>> = _uiState.asStateFlow()

    fun fetchServiceDetail(productId: Int) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {

            try {
                val serviceDetails = getServicesUseCase(productId.toString())
                _uiState.value = UiState.Success(serviceDetails)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }
}
