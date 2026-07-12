package com.gowesan.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.gowesan.app.data.model.Listing
import com.gowesan.app.navigation.Screen
import com.gowesan.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadListings() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with logo & search
        TopAppBar(
            title = {
                Text("Gowesan", fontWeight = FontWeight.Bold, color = TokopediaGreen)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            actions = {
                IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                    Icon(Icons.Filled.Search, "Cari")
                }
            }
        )

        // Quick category chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val cats = listOf("Semua" to null, "Sepeda" to "Sepeda", "Sparepart" to "Sparepart",
                "Aksesoris" to "Aksesoris", "Jersey" to "Jersey", "Lainnya" to "Lainnya")
            cats.forEach { (name, value) ->
                val selected = viewModel.selectedCategory == value
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.selectCategory(value) },
                    label = { Text(name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TokopediaGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Sort row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Terbaru" to "newest", "Termurah" to "cheapest", "Termahal" to "expensive").forEach { (label, value) ->
                val selected = viewModel.selectedSort == value
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = if (selected) TokopediaGreen else TextSecondary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.clickable { viewModel.selectSort(value) }
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TokopediaGreen)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error ?: "Error", color = SoldRed)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadListings() }) { Text("Coba Lagi") }
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.listings, key = { it.id }) { listing ->
                    ListingCard(listing = listing, onClick = {
                        navController.navigate(Screen.ListingDetail.createRoute(listing.id))
                    })
                }
            }
        }
    }
}

@Composable
fun ListingCard(listing: Listing, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = listing.primaryPhoto,
                    contentDescription = listing.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop
                )
                // Sold badge
                if (listing.status in listOf("sold", "habis")) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TERJUAL", color = SoldRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary
                )
                if (listing.price != null) {
                    Text(
                        text = "Rp ${formatPrice(listing.price)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceGreen
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.location ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ThumbUp, contentDescription = null,
                            modifier = Modifier.size(12.dp), tint = TextSecondary)
                        Text(
                            text = "${listing.likeCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatPrice(price: String): String {
    return try {
        val num = price.replace(",", "").toLongOrNull() ?: price
        if (num is Long) {
            "%,d".format(num)
        } else price
    } catch (e: Exception) {
        price
    }
}
