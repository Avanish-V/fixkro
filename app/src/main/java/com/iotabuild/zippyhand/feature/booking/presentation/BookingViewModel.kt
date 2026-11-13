package com.iotabuild.zippyhand.feature.booking.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.booking.domain.BookingRepository
import com.iotabuild.zippyhand.feature.booking.domain.MyOrderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookingsUiState {
    object Loading : BookingsUiState()
    data class Success(val bookings: List<MyOrderResponse>) : BookingsUiState()
    data class Error(val message: String) : BookingsUiState()
}

class BookingViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingsUiState>(BookingsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        fetchBookings()
    }

    fun fetchBookings() {
        viewModelScope.launch {
            _uiState.value = BookingsUiState.Loading
            performFetch()
        }
    }

    fun refreshBookings() {
        viewModelScope.launch {
            _isRefreshing.value = true
            performFetch()
            _isRefreshing.value = false
        }
    }

    private suspend fun performFetch() {
        try {
            val response = repository.getMyBookings()
            if (response.success) {
                val paidOrders = response.data.content.asReversed()
                _uiState.value = BookingsUiState.Success(paidOrders)
            } else {
                if (_uiState.value !is BookingsUiState.Success) {
                    _uiState.value = BookingsUiState.Error("Failed to fetch bookings")
                }

            }
        } catch (e: Exception) {
            if (_uiState.value !is BookingsUiState.Success) {
                _uiState.value = BookingsUiState.Error("Something went wrong!")
            }
        }
    }

    fun cancelBooking(orderId: Long, reason: String = "User Request") {
        viewModelScope.launch {
            _uiState.value = BookingsUiState.Loading
            try {
                val response = repository.cancelOrder(orderId, reason)
                if (response.success) {
                    fetchBookings()
                } else {
                    _uiState.value = BookingsUiState.Error(response.error?.message ?: "Failed to cancel order")
                }
            } catch (e: Exception) {
                _uiState.value = BookingsUiState.Error(e.message ?: "Network error")
            }
        }
    }
}
