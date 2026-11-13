package com.iotabuild.zippyhand.feature.booking.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.booking.domain.BookingRepository
import com.iotabuild.zippyhand.feature.booking.domain.MyOrderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookingDetailUiState {
    object Loading : BookingDetailUiState()
    data class Success(val order: MyOrderResponse) : BookingDetailUiState()
    data class Error(val message: String) : BookingDetailUiState()
}

class BookingDetailViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingDetailUiState>(BookingDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun fetchBookingDetail(orderId: Long) {
        viewModelScope.launch {
            _uiState.value = BookingDetailUiState.Loading
            try {
                val response = repository.getBookingDetail(orderId)
                if (response.success) {
                    _uiState.value = BookingDetailUiState.Success(response.data)
                    Log.d("BookingDetailViewModel", "Order Details: ${response.data}")
                } else {
                    _uiState.value = BookingDetailUiState.Error(response.error?.message ?: "Failed to fetch order details")
                }
            } catch (e: Exception) {
                _uiState.value = BookingDetailUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun cancelBooking(orderId: Long, reason: String = "User Request") {
        viewModelScope.launch {
            _uiState.value = BookingDetailUiState.Loading
            try {
                val response = repository.cancelOrder(orderId, reason)
                if (response.success) {
                    _uiState.value = BookingDetailUiState.Success(response.data)
                } else {
                    _uiState.value = BookingDetailUiState.Error(response.error?.message ?: "Failed to cancel order")
                }
            } catch (e: Exception) {
                _uiState.value = BookingDetailUiState.Error(e.message ?: "Network error")
            }
        }
    }
}
