package com.gowesan.app.ui.listing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gowesan.app.data.model.Listing
import com.gowesan.app.data.model.Owner
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.navigation.Screen
import com.gowesan.app.ui.home.formatPrice
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerListingsViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    // We'll use the HTTP-based seller page in production;
    // for now load all and filter client-side
    private val _activeListings = MutableStateFlow<List<Listing>>(emptyList())
    val activeListings: StateFlow<List<Listing>> = _activeListings
    private val _soldListings = MutableStateFlow<List<Listing>>(emptyList())
    val soldListings: StateFlow<List<Listing>> = _soldListings
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    private val _sellerCount = MutableStateFlow(0)
    val sellerCount: StateFlow<Int> = _sellerCount

    fun load(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Check count first
                val countR = repo.getSellerListingCount(userId)
                if (countR.isSuccessful) {
                    val count = countR.body()?.count ?: 0
                    _sellerCount.value = count
                }
                // For now we load all listings; ideally use a dedicated seller API
                val r = repo.getListings(page = 1)
                if (r.isSuccessful) {
                    val all = r.body()?.listings ?: emptyList()
                    val sellerItems = all.filter { it.owner?.id == userId }
                    _activeListings.value = sellerItems.filter { it.status == "published" }
                    _soldListings.value = sellerItems.filter { it.status in listOf("sold", "habis") }
                }
            } catch (_: Exception) {}
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerListingsScreen(navController: NavController, userId: String,
                         viewModel: SellerListingsViewModel = hiltViewModel()) {
    val active by viewModel.activeListings.collectAsState()
    val sold by viewModel.soldListings.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val count by viewModel.sellerCount.collectAsState()

    LaunchedEffect(userId) { viewModel.load(userId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Dagangan Penjual", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TokopediaGreen)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Summary
                Card(colors = CardDefaults.cardColors(containerColor = DarkCard),
                    modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${active.size + sold.size}", fontWeight = FontWeight.Bold,
                                color = TextPrimary, fontSize = 18.sp)
                            Text("Total", color = TextSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${active.size}", fontWeight = FontWeight.Bold,
                                color = TokopediaGreen, fontSize = 18.sp)
                            Text("Tersedia", color = TextSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${sold.size}", fontWeight = FontWeight.Bold,
                                color = SoldRed, fontSize = 18.sp)
                            Text("Terjual", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // Active section
                if (active.isNotEmpty()) {
                    Text("Sedang Dijual", fontWeight = FontWeight.Bold, color = TokopediaGreen,
                        modifier = Modifier.padding(horizontal = 12.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(2),
                        modifier = Modifier.heightIn(max = 400.dp).padding(8.dp)) {
                        items(active) { l ->
                            SellerListingItem(l) {
                                navController.navigate(Screen.ListingDetail.createRoute(l.id))
                            }
                        }
                    }
                }

                // Sold section
                if (sold.isNotEmpty()) {
                    Text("Terjual", fontWeight = FontWeight.Bold, color = SoldRed,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(2),
                        modifier = Modifier.padding(8.dp)) {
                        items(sold) { l ->
                            SellerListingItem(l, isSold = true) {
                                navController.navigate(Screen.ListingDetail.createRoute(l.id))
                            }
                        }
                    }
                }

                if (active.isEmpty() && sold.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Penjual ini belum punya dagangan.", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun SellerListingItem(listing: Listing, isSold: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Box {
            AsyncImage(
                model = listing.primaryPhoto,
                contentDescription = listing.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )
            if (isSold) {
                Box(modifier = Modifier.fillMaxSize().then(
                    Modifier.background(Color.Black.copy(alpha = 0.5f))),
                    contentAlignment = Alignment.Center) {
                    Text("TERJUAL", color = SoldRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        Column(modifier = Modifier.padding(8.dp)) {
            Text(listing.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = TextPrimary, fontSize = 12.sp)
            if (listing.price != null) {
                Text("Rp ${formatPrice(listing.price)}", color = PriceGreen,
                    fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
