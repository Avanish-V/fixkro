package com.iotabuild.zippyhand.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.home.data.remote.HomeResponse
import com.iotabuild.zippyhand.feature.home.domain.HomeRepository
import com.iotabuild.zippyhand.feature.servicecategory.domain.Category
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceRepository
import com.iotabuild.zippyhand.location.domain.LocationRepository
import com.iotabuild.zippyhand.utils.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    @Suppress("unused") private val serviceRepository: ServiceRepository,
    private val homeRepository: HomeRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private var serviceJob: Job? = null

    private val _homeState = MutableStateFlow<UiState<HomeResponse>>(UiState.Idle)
    val homeState: StateFlow<UiState<HomeResponse>> = _homeState.asStateFlow()

    val location = locationRepository.observeLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData(forceRefresh: Boolean = false) {
        _homeState.value = UiState.Loading
        viewModelScope.launch {
            homeRepository.getHomeData(forceRefresh).fold(
                onSuccess = { response ->
                    val initialCategory = response.categories.firstOrNull()
                    _selectedCategory.value = initialCategory
                    _homeState.value = UiState.Success(response)
                },
                onFailure = { error ->
                    _homeState.value = UiState.Error(error.message ?: "Failed to load home data")
                }
            )
        }
    }

    fun onCategorySelected(category: Category) {
        _selectedCategory.value = category
        val currentSuccess = _homeState.value as? UiState.Success ?: return

        serviceJob?.cancel()

        val isCached = homeRepository.isCategoryCached(category.categoryId)

        // Only show service shimmer if the category services are NOT cached yet
        if (!isCached) {
            _homeState.value = UiState.Success(
                currentSuccess.data.copy(isServiceLoading = true, isError = false)
            )
        }

        serviceJob = viewModelScope.launch {
            homeRepository.getServicesForCategory(category.categoryId, forceRefresh = false).fold(
                onSuccess = { servicesList ->
                    val activeData = (_homeState.value as? UiState.Success)?.data ?: return@launch
                    _homeState.value = UiState.Success(
                        activeData.copy(
                            services = servicesList,
                            isServiceLoading = false,
                            isError = false
                        )
                    )
                },
                onFailure = { _ ->
                    val activeData = (_homeState.value as? UiState.Success)?.data ?: return@launch
                    _homeState.value = UiState.Success(
                        activeData.copy(
                            isServiceLoading = false,
                            isError = true
                        )
                    )
                }
            )
        }
    }

    fun onRetry() {
        loadHomeData(forceRefresh = true)
    }
}
