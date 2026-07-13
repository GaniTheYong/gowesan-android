package com.gowesan.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gowesan.app.R
import com.gowesan.app.data.model.Article
import com.gowesan.app.data.model.Listing
import com.gowesan.app.data.model.Place
import com.gowesan.app.data.model.SearchResponse
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.navigation.Screen
import com.gowesan.app.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel

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

    fun clear() { _result.value = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchResult by searchViewModel.result.collectAsState()
    val searchLoading by searchViewModel.loading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAll() }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── TopAppBar with Logo + Inline Search ──
        if (searchExpanded) {
            // Search mode
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        searchExpanded = false
                        searchQuery = ""
                        searchViewModel.clear()
                    }) { Icon(Icons.Filled.ArrowBack, "Tutup") }
                },
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (it.length >= 2) searchViewModel.search(it)
                            else if (it.isEmpty()) searchViewModel.clear()
                        },
                        placeholder = { Text("Cari sepeda, sparepart...", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TokopediaGreen,
                            cursorColor = TokopediaGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = ""; searchViewModel.clear() }) {
                                    Icon(Icons.Filled.Close, "Hapus", tint = TextSecondary)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        } else {
            // Normal mode — logo + search icon
            TopAppBar(
                title = {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.logo_gowesan),
                        contentDescription = "Gowesan",
                        modifier = Modifier.height(40.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
                actions = {
                    IconButton(onClick = { searchExpanded = true }) {
                        Icon(Icons.Filled.Search, "Cari")
                    }
                }
            )
        }

        // ── Search Results / Main Content ──
        if (searchExpanded) {
            if (searchLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TokopediaGreen)
                }
            } else if (searchResult != null) {
                LazyColumn(modifier = Modifier.padding(12.dp)) {
                    val data = searchResult!!
                    if (data.listings.isNotEmpty()) {
                        item { Text("Listing", fontWeight = FontWeight.Bold, color = TokopediaGreen, modifier = Modifier.padding(bottom = 4.dp)) }
                        items(data.listings) { l ->
                            Text("🛒 ${l.title} — Rp ${l.price ?: "-"}", color = TextPrimary,
                                modifier = Modifier.clickable {
                                    searchExpanded = false; searchQuery = ""; searchViewModel.clear()
                                    navController.navigate(Screen.ListingDetail.createRoute(l.id))
                                }.padding(vertical = 4.dp))
                        }
                    }
                    if (data.events.isNotEmpty()) {
                        item { Text("Event", fontWeight = FontWeight.Bold, color = Orange500, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                        items(data.events) { e ->
                            Text("📅 ${e.title}", color = TextPrimary,
                                modifier = Modifier.clickable {
                                    searchExpanded = false; searchQuery = ""; searchViewModel.clear()
                                    navController.navigate(Screen.EventDetail.createRoute(e.id))
                                }.padding(vertical = 4.dp))
                        }
                    }
                    if (data.communities.isNotEmpty()) {
                        item { Text("Komunitas", fontWeight = FontWeight.Bold, color = LikeBlue, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                        items(data.communities) { c ->
                            Text("👥 ${c.name} — ${c.city ?: ""}", color = TextPrimary,
                                modifier = Modifier.clickable {
                                    searchExpanded = false; searchQuery = ""; searchViewModel.clear()
                                    navController.navigate(Screen.CommunityDetail.createRoute(c.id))
                                }.padding(vertical = 4.dp))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ketik untuk mencari...", color = TextSecondary)
                }
            }
        } else {
            // ── Main Content ──
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TokopediaGreen)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error ?: "Error", color = SoldRed)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.loadAll() },
                            colors = ButtonDefaults.buttonColors(containerColor = TokopediaGreen)
                        ) { Text("Coba Lagi") }
                    }
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                ) {
                    // ── Hero Banner ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TokopediaGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚴 Komunitas Pesepeda Indonesia",
                                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TokopediaGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Event, marketplace, tips & tempat nongkrong favorit.",
                                fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    // ── Marketplace ──
                    SectionHeader("Marketplace Terbaru", onSeeAll = { navController.navigate(Screen.Listings.route) })
                    if (uiState.listings.isEmpty()) EmptySection("Belum ada listing")
                    else HorizontalCardList {
                        uiState.listings.take(8).forEach { listing ->
                            ListingCard(listing, onClick = { navController.navigate(Screen.ListingDetail.createRoute(listing.id)) })
                        }
                    }

                    // ── Tempat Bagus ──
                    SectionHeader("Tempat Bagus", onSeeAll = { navController.navigate(Screen.Places.route) })
                    if (uiState.places.isEmpty()) EmptySection("Belum ada tempat")
                    else HorizontalCardList {
                        uiState.places.take(8).forEach { place ->
                            PlaceCard(place, onClick = { navController.navigate(Screen.PlaceDetail.createRoute(place.id)) })
                        }
                    }

                    // ── Tips ──
                    SectionHeader("Tips Bersepeda", onSeeAll = { navController.navigate(Screen.Articles.route) })
                    if (uiState.articles.isEmpty()) EmptySection("Belum ada artikel")
                    else HorizontalCardList {
                        uiState.articles.take(8).forEach { article ->
                            ArticleCard(article, onClick = { navController.navigate(Screen.ArticleDetail.createRoute(article.id)) })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── Reusable Components ──

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
        TextButton(onClick = onSeeAll) { Text("Lihat Semua", color = TokopediaGreen, fontSize = 13.sp) }
    }
}

@Composable
fun HorizontalCardList(content: @Composable () -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item { content() }
    }
}

@Composable
fun EmptySection(text: String) {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        Text(text, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
fun ListingCard(listing: Listing, onClick: () -> Unit) {
    Card(modifier = Modifier.width(150.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column {
            Box {
                AsyncImage(model = listing.primaryPhoto, contentDescription = listing.title,
                    modifier = Modifier.fillMaxWidth().height(130.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                    contentScale = ContentScale.Crop)
                if (listing.status in listOf("sold", "habis")) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center) {
                        Text("TERJUAL", color = SoldRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(listing.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp, color = TextPrimary)
                if (listing.price != null) {
                    Text("Rp ${formatPrice(listing.price)}",
                        fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PriceGreen)
                }
                Text(listing.location ?: "", fontSize = 10.sp, color = TextSecondary, maxLines = 1)
            }
        }
    }
}

@Composable
fun PlaceCard(place: Place, onClick: () -> Unit) {
    Card(modifier = Modifier.width(180.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column {
            AsyncImage(model = place.thumbnailUrl, contentDescription = place.name,
                modifier = Modifier.fillMaxWidth().height(110.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(8.dp)) {
                Text(place.name, fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, color = TextPrimary, maxLines = 1)
                if (place.city != null) Text(place.city, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                if (place.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, modifier = Modifier.size(12.dp), tint = Orange500)
                        Text(" ${place.rating}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: Article, onClick: () -> Unit) {
    Card(modifier = Modifier.width(200.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column {
            AsyncImage(model = article.thumbnailUrl, contentDescription = article.title,
                modifier = Modifier.fillMaxWidth().height(100.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(8.dp)) {
                Text(article.title, fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Oleh ${article.authorName ?: "Admin"}", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

fun formatPrice(price: String): String {
    return try {
        val num = price.replace(",", "").toLongOrNull() ?: price
        if (num is Long) "%,d".format(num) else price
    } catch (e: Exception) { price }
}
