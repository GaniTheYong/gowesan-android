package com.gowesan.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gowesan.app.data.model.Article
import com.gowesan.app.data.model.Listing
import com.gowesan.app.data.model.Place
import com.gowesan.app.navigation.Screen
import com.gowesan.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAll() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Gowesan", fontWeight = FontWeight.Bold, color = TokopediaGreen) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            actions = {
                IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                    Icon(Icons.Filled.Search, "Cari")
                }
            }
        )

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

                // ── Section: Marketplace ──
                SectionHeader(
                    title = "Marketplace Terbaru",
                    onSeeAll = { navController.navigate(Screen.Listings.route) }
                )
                if (uiState.listings.isEmpty()) {
                    EmptySection("Belum ada listing")
                } else {
                    HorizontalCardList {
                        uiState.listings.take(8).forEach { listing ->
                            ListingCard(listing = listing, onClick = {
                                navController.navigate(Screen.ListingDetail.createRoute(listing.id))
                            })
                        }
                    }
                }

                // ── Section: Tempat Bagus ──
                SectionHeader(
                    title = "Tempat Bagus",
                    onSeeAll = { navController.navigate(Screen.Places.route) }
                )
                if (uiState.places.isEmpty()) {
                    EmptySection("Belum ada tempat")
                } else {
                    HorizontalCardList {
                        uiState.places.take(8).forEach { place ->
                            PlaceCard(place = place, onClick = {
                                navController.navigate(Screen.PlaceDetail.createRoute(place.id))
                            })
                        }
                    }
                }

                // ── Section: Tips ──
                SectionHeader(
                    title = "Tips Bersepeda",
                    onSeeAll = { navController.navigate(Screen.Articles.route) }
                )
                if (uiState.articles.isEmpty()) {
                    EmptySection("Belum ada artikel")
                } else {
                    HorizontalCardList {
                        uiState.articles.take(8).forEach { article ->
                            ArticleCard(article = article, onClick = {
                                navController.navigate(Screen.ArticleDetail.createRoute(article.id))
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Reusable Section Components ──

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
        TextButton(onClick = onSeeAll) {
            Text("Lihat Semua", color = TokopediaGreen, fontSize = 13.sp)
        }
    }
}

@Composable
fun HorizontalCardList(content: @Composable () -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { content() }
    }
}

@Composable
fun EmptySection(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextSecondary, fontSize = 13.sp)
    }
}

// ── Listing Card ──

@Composable
fun ListingCard(listing: Listing, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = listing.primaryPhoto,
                    contentDescription = listing.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                    contentScale = ContentScale.Crop
                )
                if (listing.status in listOf("sold", "habis")) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) { Text("TERJUAL", color = SoldRed, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(listing.title,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp, color = TextPrimary)
                if (listing.price != null) {
                    Text("Rp ${formatPrice(listing.price)}",
                        fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PriceGreen)
                }
                Text(listing.location ?: "",
                    fontSize = 10.sp, color = TextSecondary, maxLines = 1)
            }
        }
    }
}

// ── Place Card ──

@Composable
fun PlaceCard(place: Place, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column {
            AsyncImage(
                model = place.thumbnailUrl,
                contentDescription = place.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(place.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, color = TextPrimary, maxLines = 1)
                if (place.city != null) {
                    Text(place.city,
                        fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                }
                if (place.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null,
                            modifier = Modifier.size(12.dp), tint = Orange500)
                        Text(" ${place.rating}",
                            fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ── Article Card ──

@Composable
fun ArticleCard(article: Article, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column {
            AsyncImage(
                model = article.thumbnailUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(article.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, color = TextPrimary,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Oleh ${article.authorName ?: "Admin"}",
                    fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

// ── Format Helper ──

fun formatPrice(price: String): String {
    return try {
        val num = price.replace(",", "").toLongOrNull() ?: price
        if (num is Long) "%,d".format(num) else price
    } catch (e: Exception) { price }
}
