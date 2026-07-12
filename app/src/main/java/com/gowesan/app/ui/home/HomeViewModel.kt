package com.gowesan.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gowesan.app.data.model.Article
import com.gowesan.app.data.model.Listing
import com.gowesan.app.data.model.Place
import com.gowesan.app.data.repository.GowesanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val listings: List<Listing> = emptyList(),
    val places: List<Place> = emptyList(),
    val articles: List<Article> = emptyList(),
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

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val listingResp = repository.getListings(page = 1, category = selectedCategory, sort = selectedSort)
                val placesResp = repository.getPlaces()
                val articlesResp = repository.getArticles(page = 1)

                val listings = if (listingResp.isSuccessful) listingResp.body()?.listings ?: emptyList() else emptyList()
                val places = if (placesResp.isSuccessful) placesResp.body()?.places ?: emptyList() else emptyList()
                val articles = if (articlesResp.isSuccessful) articlesResp.body()?.articles ?: emptyList() else emptyList()

                _uiState.value = _uiState.value.copy(
                    listings = listings,
                    places = places,
                    articles = articles,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Gagal memuat data. Periksa koneksi Anda."
                )
            }
        }
    }

    fun selectCategory(category: String?) {
        selectedCategory = category
        currentPage = 1
        loadAll()
    }

    fun selectSort(sort: String) {
        selectedSort = sort
        currentPage = 1
        loadAll()
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
