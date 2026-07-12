package com.gowesan.app.ui.transaksi

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.gowesan.app.data.model.Listing
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
class TransaksiViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val r = repo.getMyListings()
                if (r.isSuccessful) _listings.value = r.body()?.listings ?: emptyList()
            } catch (_: Exception) {}
            _loading.value = false
        }
    }

    fun stopListing(id: String) {
        viewModelScope.launch {
            try { repo.stopListing(id); load() } catch (_: Exception) {}
        }
    }

    fun markHabis(id: String) {
        viewModelScope.launch {
            try { repo.markHabis(id); load() } catch (_: Exception) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiScreen(navController: NavController, viewModel: TransaksiViewModel = hiltViewModel()) {
    val listings by viewModel.listings.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Dagangan Saya", fontWeight = FontWeight.Bold, color = TokopediaGreen) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            actions = {
                IconButton(onClick = { navController.navigate(Screen.CreateListing.route) }) {
                    Icon(Icons.Filled.Add, "Pasang Baru", tint = TokopediaGreen)
                }
            }
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TokopediaGreen)
            }
        } else if (listings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Inbox, null, modifier = Modifier.size(64.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Belum ada dagangan", color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigate(Screen.CreateListing.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = TokopediaGreen)) {
                        Text("Pasang Dagangan")
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(listings, key = { it.id }) { l ->
                    TransactionCard(listing = l, navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(listing: Listing, navController: NavController, viewModel: TransaksiViewModel) {
    val statusColor = when (listing.status) {
        "published" -> TokopediaGreen
        "draft" -> TextSecondary
        "waiting_payment" -> Orange500
        "waiting_approval" -> LikeBlue
        "rejected" -> SoldRed
        "sold", "habis" -> SoldRed
        else -> TextSecondary
    }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(listing.title, fontWeight = FontWeight.Bold, color = TextPrimary,
                    modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(listing.status.uppercase(), fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = statusColor.copy(alpha = 0.2f),
                        labelColor = statusColor))
            }
            if (listing.price != null) {
                Text("Rp ${formatPrice(listing.price)}", color = PriceGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (listing.status == "draft") {
                    TextButton(onClick = { /* redirect to web or edit */ }) { Text("Edit") }
                    TextButton(onClick = {
                        navController.navigate(Screen.Invoice.createRoute(listing.id))
                    }) { Text("Publish", color = TokopediaGreen) }
                }
                if (listing.status == "waiting_payment") {
                    TextButton(onClick = {
                        navController.navigate(Screen.Invoice.createRoute(listing.id))
                    }) { Text("Bayar", color = TokopediaGreen) }
                }
                if (listing.status == "published") {
                    TextButton(onClick = { viewModel.markHabis(listing.id) }) { Text("Tandai Habis") }
                    TextButton(onClick = { viewModel.stopListing(listing.id) }) { Text("Stop", color = SoldRed) }
                }
                if (listing.status == "sold" || listing.status == "habis") {
                    TextButton(onClick = { /* rate seller */ }) { Text("Rate", color = Orange500) }
                }
            }
        }
    }
}
