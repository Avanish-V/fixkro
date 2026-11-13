package com.iotabuild.zippyhand.feature.offers.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.offers.domain.Offer
import com.iotabuild.zippyhand.feature.offers.domain.OfferRepository
import com.iotabuild.zippyhand.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OffersViewModel(
    private val offerRepository: OfferRepository
) : ViewModel() {

    private val _offers = MutableStateFlow<UiState<List<Offer>>>(UiState.Loading)
    val offers: StateFlow<UiState<List<Offer>>> = _offers

    init {
        fetchOffers()
    }

    fun fetchOffers() {
        viewModelScope.launch {
            _offers.value = UiState.Loading
            try {
                val response = offerRepository.getOffers()
                Log.d("OffersViewModel", "Response: $response")
                if (response.success) {
                    _offers.value = UiState.Success(response.data)
                } else {
                    _offers.value = UiState.Error(response.error?.message ?: "Failed to load offers")
                }
            } catch (e: Exception) {
                _offers.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
