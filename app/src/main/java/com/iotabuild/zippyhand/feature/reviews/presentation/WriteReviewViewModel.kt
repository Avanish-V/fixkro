package com.iotabuild.zippyhand.feature.reviews.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.reviews.domain.CreateReviewRequest
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WriteReviewUiState {
    object Idle : WriteReviewUiState()
    object Loading : WriteReviewUiState()
    object Success : WriteReviewUiState()
    data class Error(val message: String) : WriteReviewUiState()
}

class WriteReviewViewModel(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WriteReviewUiState>(WriteReviewUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun submitReview(
        productId: Long,
        orderId: Long,
        customerName: String,
        rating: Int,
        technicianRating: Int,
        technicianId: Long,
        isRecommended: Boolean,
        impressions: Set<String>,
        comment: String
    ) {
        viewModelScope.launch {
            _uiState.value = WriteReviewUiState.Loading
            try {
                val request = CreateReviewRequest(
                    productId = productId,
                    orderId = orderId,
                    customerName = customerName,
                    rating = rating,
                    technicianRating = technicianRating,
                    technicianId = technicianId,
                    isRecommended = isRecommended,
                    impressions = impressions,
                    comment = comment
                )
                val response = reviewRepository.createReview(request)
                if (response.success) {
                    _uiState.value = WriteReviewUiState.Success
                } else {
                    Log.e("WriteReviewViewModel", "Failed to submit review: ${response.error?.message}")
                    _uiState.value = WriteReviewUiState.Error(response.error?.message ?: "Failed to submit review")
                }
            } catch (e: Exception) {
                Log.e("WriteReviewViewModel", "Error submitting review", e)
                _uiState.value = WriteReviewUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = WriteReviewUiState.Idle
    }
}
