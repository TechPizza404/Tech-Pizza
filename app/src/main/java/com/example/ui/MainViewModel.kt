package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.CompareResponse
import com.example.api.GeminiClient
import com.example.api.PhoneDetailResponse
import com.example.api.SearchResponseItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- States for the three primary tabs of TechX ---

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val results: List<SearchResponseItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

sealed interface DetailUiState {
    object Idle : DetailUiState
    object Loading : DetailUiState
    data class Success(val detail: PhoneDetailResponse) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

sealed interface CompareUiState {
    object Idle : CompareUiState
    object Loading : CompareUiState
    data class Success(val comparison: CompareResponse) : CompareUiState
    data class Error(val message: String) : CompareUiState
}

class MainViewModel : ViewModel() {

    // Search and Browse state
    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Phone Specs State
    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    private val _selectedPhone = MutableStateFlow("")
    val selectedPhone: StateFlow<String> = _selectedPhone.asStateFlow()

    // Comparison State
    private val _compareState = MutableStateFlow<CompareUiState>(CompareUiState.Idle)
    val compareState: StateFlow<CompareUiState> = _compareState.asStateFlow()

    private val _phoneA = MutableStateFlow("")
    val phoneA: StateFlow<String> = _phoneA.asStateFlow()

    private val _phoneB = MutableStateFlow("")
    val phoneB: StateFlow<String> = _phoneB.asStateFlow()

    // Predefined popular phones list for high-fidelity clickability
    val popularPhones = listOf(
        "iPhone 15 Pro Max",
        "Google Pixel 8 Pro",
        "Samsung Galaxy S24 Ultra",
        "OnePlus 12",
        "Sony Xperia 1 VI",
        "Xiaomi 14 Ultra"
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchPhones(query: String) {
        if (query.trim().isEmpty()) return
        _searchQuery.value = query
        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            try {
                val results = GeminiClient.searchPhones(query)
                _searchState.value = SearchUiState.Success(results)
            } catch (e: Exception) {
                _searchState.value = SearchUiState.Error(e.message ?: "An unknown network error occurred.")
            }
        }
    }

    fun updateSelectedPhone(model: String) {
        _selectedPhone.value = model
    }

    fun loadPhoneSpecs(model: String) {
        if (model.trim().isEmpty()) return
        _selectedPhone.value = model
        viewModelScope.launch {
            _detailState.value = DetailUiState.Loading
            try {
                val response = GeminiClient.getPhoneDetail(model)
                _detailState.value = DetailUiState.Success(response)
            } catch (e: Exception) {
                _detailState.value = DetailUiState.Error(e.message ?: "Could not retrieve phone details.")
            }
        }
    }

    fun updatePhoneA(model: String) {
        _phoneA.value = model
    }

    fun updatePhoneB(model: String) {
        _phoneB.value = model
    }

    fun executeComparison(phoneA: String, phoneB: String) {
        if (phoneA.trim().isEmpty() || phoneB.trim().isEmpty()) return
        _phoneA.value = phoneA
        _phoneB.value = phoneB
        viewModelScope.launch {
            _compareState.value = CompareUiState.Loading
            try {
                val response = GeminiClient.comparePhones(phoneA, phoneB)
                _compareState.value = CompareUiState.Success(response)
            } catch (e: Exception) {
                _compareState.value = CompareUiState.Error(e.message ?: "Comparison model generation failed.")
            }
        }
    }
}
