package com.gowesan.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gowesan.app.data.model.Listing
import com.gowesan.app.data.repository.GowesanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GowesanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    var selectedCategory: String? = null
    var selectedSort: String = "newest"
    private var currentPage = 1

    fun loadListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = repository.getListings(
                    page = 1,
                    category = selectedCategory,
                    sort = selectedSort
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        listings = response.body()?.listings ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal memuat listing (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Koneksi error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun selectCategory(category: String?) {
        selectedCategory = category
        currentPage = 1
        loadListings()
    }

    fun selectSort(sort: String) {
        selectedSort = sort
        currentPage = 1
        loadListings()
    }

    fun loadMore() {
        viewModelScope.launch {
            currentPage++
            try {
                val response = repository.getListings(page = currentPage, category = selectedCategory, sort = selectedSort)
                if (response.isSuccessful) {
                    val newListings = response.body()?.listings ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        listings = _uiState.value.listings + newListings
                    )
                }
            } catch (_: Exception) {}
        }
    }
}
