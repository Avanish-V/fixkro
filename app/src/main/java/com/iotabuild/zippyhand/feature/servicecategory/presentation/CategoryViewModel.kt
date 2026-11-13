package com.iotabuild.zippyhand.feature.servicecategory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iotabuild.zippyhand.feature.servicecategory.domain.Category
import com.iotabuild.zippyhand.feature.servicecategory.domain.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CategoryUiState {
    object Loading : CategoryUiState()
    data class Success(val categories: List<Category>) : CategoryUiState()
    data class Error(val message: String) : CategoryUiState()
}

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading
            try {
                val response = repository.getCategories()
                if (response.success) {
                    _uiState.value = CategoryUiState.Success(response.data)
                } else {
                    _uiState.value = CategoryUiState.Error(response.error?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = CategoryUiState.Error(e.message ?: "Network error")
            }
        }
    }
}
