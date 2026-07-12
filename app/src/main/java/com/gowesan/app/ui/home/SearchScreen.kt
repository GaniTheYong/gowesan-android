package com.gowesan.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gowesan.app.data.model.SearchResponse
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.navigation.Screen
import com.gowesan.app.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    private val _result = MutableStateFlow<SearchResponse?>(null)
    val result: StateFlow<SearchResponse?> = _result
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun search(query: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val r = repo.search(query)
                if (r.isSuccessful) _result.value = r.body()
            } catch (_: Exception) {}
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: SearchViewModel = hiltViewModel()) {
    var query by remember { mutableStateOf("") }
    val result by viewModel.result.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Cari sepeda, sparepart...", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TokopediaGreen,
                        cursorColor = TokopediaGreen,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { viewModel.search(query) }) {
                                Icon(Icons.Filled.Search, "Cari", tint = TokopediaGreen)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, "Kembali")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TokopediaGreen)
            }
        } else if (result != null) {
            LazyColumn(modifier = Modifier.padding(12.dp)) {
                val data = result!!
                if (data.listings.isNotEmpty()) {
                    item { Text("Listing", fontWeight = FontWeight.Bold, color = TokopediaGreen, modifier = Modifier.padding(bottom = 4.dp)) }
                    items(data.listings) { l ->
                        Text("🛒 ${l.title} — Rp ${l.price ?: "-"}", color = TextPrimary,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.ListingDetail.createRoute(l.id))
                            }.padding(vertical = 4.dp))
                    }
                }
                if (data.events.isNotEmpty()) {
                    item { Text("Event", fontWeight = FontWeight.Bold, color = Orange500, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                    items(data.events) { e ->
                        Text("📅 ${e.title}", color = TextPrimary,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.EventDetail.createRoute(e.id))
                            }.padding(vertical = 4.dp))
                    }
                }
                if (data.communities.isNotEmpty()) {
                    item { Text("Komunitas", fontWeight = FontWeight.Bold, color = LikeBlue, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                    items(data.communities) { c ->
                        Text("👥 ${c.name} — ${c.city ?: ""}", color = TextPrimary,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.CommunityDetail.createRoute(c.id))
                            }.padding(vertical = 4.dp))
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ketik kata kunci dan tekan cari", color = TextSecondary)
            }
        }
    }
}
