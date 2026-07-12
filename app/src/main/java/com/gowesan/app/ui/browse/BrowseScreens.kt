package com.gowesan.app.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gowesan.app.data.model.Article
import com.gowesan.app.data.model.Place
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.ui.home.formatPrice
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class BrowseViewModel @Inject constructor(
    private val repo: GowesanRepository
) : ViewModel() {
    var items by mutableStateOf(listOf<Any>())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun loadPlaces() {
        viewModelScope.launch {
            isLoading = true
            try {
                val r = repo.getPlaces()
                items = if (r.isSuccessful) r.body()?.places ?: emptyList() else emptyList()
            } catch (_: Exception) { error = "Gagal memuat" }
            isLoading = false
        }
    }

    fun loadArticles() {
        viewModelScope.launch {
            isLoading = true
            try {
                val r = repo.getArticles()
                items = if (r.isSuccessful) r.body()?.articles ?: emptyList() else emptyList()
            } catch (_: Exception) { error = "Gagal memuat" }
            isLoading = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesBrowseScreen(navController: NavController, vm: BrowseViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) { vm.loadPlaces() }
    BrowseScaffold(navController, "Tempat Bagus") {
        if (vm.isLoading) CircularProgressIndicator(color = TokopediaGreen, modifier = Modifier.align(Alignment.Center))
        else LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            @Suppress("UNCHECKED_CAST")
            items(vm.items.filterIsInstance<Place>()) { place ->
                PlaceRow(place)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesBrowseScreen(navController: NavController, vm: BrowseViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) { vm.loadArticles() }
    BrowseScaffold(navController, "Tips Bersepeda") {
        if (vm.isLoading) CircularProgressIndicator(color = TokopediaGreen, modifier = Modifier.align(Alignment.Center))
        else LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            @Suppress("UNCHECKED_CAST")
            items(vm.items.filterIsInstance<Article>()) { article ->
                ArticleRow(article)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScaffold(navController: NavController, title: String, content: @Composable BoxScope.() -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = TokopediaGreen) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Kembali") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) { content() }
    }
}

@Composable
fun PlaceRow(place: Place) {
    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(place.thumbnailUrl, null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(place.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                if (place.city != null) Text(place.city, color = TextSecondary, fontSize = 12.sp)
                if (place.rating != null) Text("⭐ ${place.rating}", color = Orange500, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ArticleRow(article: Article) {
    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(article.thumbnailUrl, null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(article.title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Oleh ${article.authorName ?: "Admin"}", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
