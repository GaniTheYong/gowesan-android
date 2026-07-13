package com.gowesan.app.ui.listing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gowesan.app.data.model.Listing
import com.gowesan.app.data.model.ReportRequest
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
class ListingDetailViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    private val _listing = MutableStateFlow<Listing?>(null)
    val listing: StateFlow<Listing?> = _listing
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val r = repo.getListingDetail(id)
                if (r.isSuccessful) _listing.value = r.body()?.listing
                else _error.value = "Gagal memuat listing"
            } catch (e: Exception) { _error.value = e.localizedMessage }
            _loading.value = false
        }
    }

    fun report(listingId: String, name: String, desc: String) {
        viewModelScope.launch {
            try { repo.reportListing(listingId, name, "", desc) } catch (_: Exception) {}
        }
    }

    fun submitRating(listingId: String, rating: Int, testimoni: String) {
        viewModelScope.launch {
            try { repo.rateSeller(listingId, rating, testimoni) } catch (_: Exception) {}
        }
    }

    fun toggleLike(id: String, type: String) {
        viewModelScope.launch {
            try {
                repo.toggleLike(id, type)
                load(id) // reload
            } catch (_: Exception) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(navController: NavController, listingId: String,
                        viewModel: ListingDetailViewModel = hiltViewModel()) {
    val listing by viewModel.listing.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(5) }
    var testimoni by remember { mutableStateOf("") }
    var reportName by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }

    LaunchedEffect(listingId) { viewModel.load(listingId) }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TokopediaGreen)
        }
        return
    }

    if (error != null || listing == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error ?: "Listing tidak ditemukan", color = SoldRed)
        }
        return
    }

    val l = listing!!

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Back button
        Row(modifier = Modifier.padding(8.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali", tint = TextPrimary)
            }
        }

        // Gallery
        if (l.photos?.isNotEmpty() == true) {
            AsyncImage(
                model = l.photos.first(),
                contentDescription = l.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(l.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))

            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(l.category, fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = DarkCard))
                AssistChip(onClick = {}, label = { Text(l.condition, fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = DarkCard))
                if (l.status in listOf("sold", "habis")) {
                    AssistChip(onClick = {}, label = { Text("TERJUAL", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = SoldRed))
                }
                if (l.location != null) {
                    AssistChip(onClick = {}, label = { Text(l.location, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkCard))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Price
            if (l.price != null) {
                Text("Rp ${formatPrice(l.price)}", color = PriceGreen,
                    fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Description
            if (!l.description.isNullOrBlank()) {
                Text("Deskripsi", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(l.description!!, color = TextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Online store link
            if (!l.onlineStoreUrl.isNullOrBlank()) {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(l.onlineStoreUrl))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lihat di Toko Online")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Like / Dislike
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = { viewModel.toggleLike(l.id, "like") },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkCard)) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = LikeBlue, modifier = Modifier.size(16.dp))
                    Text(" ${l.likeCount}")
                }
                FilledTonalButton(onClick = { viewModel.toggleLike(l.id, "dislike") },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkCard)) {
                    Icon(Icons.Filled.ThumbDown, contentDescription = null, tint = SoldRed, modifier = Modifier.size(16.dp))
                    Text(" ${l.dislikeCount}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Seller card
            Card(colors = CardDefaults.cardColors(containerColor = DarkCard), shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Penjual", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (l.owner?.avatarUrl != null) {
                            AsyncImage(model = l.owner!!.avatarUrl, contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = null,
                                modifier = Modifier.size(48.dp), tint = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(l.owner?.displayName ?: l.owner?.username ?: "-",
                                fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(l.owner?.city ?: "", color = TextSecondary, fontSize = 13.sp)
                            if (l.avgRating != null) {
                                Row {
                                    Icon(Icons.Filled.Star, contentDescription = null,
                                        tint = Orange500, modifier = Modifier.size(14.dp))
                                    Text(" ${l.avgRating}", color = Orange500, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // WhatsApp button
                    if (l.owner?.phone != null) {
                        val waNumber = l.owner!!.phone!!.replace("+", "").replace(" ", "")
                        val waUrl = "https://wa.me/$waNumber?text=Halo, saya tertarik dengan listing ${l.title}"
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                            context.startActivity(intent)
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chat WhatsApp", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Lihat dagangan lain button
                    OutlinedButton(onClick = {
                        if (l.owner != null) {
                            navController.navigate(Screen.SellerListings.createRoute(l.owner!!.id))
                        }
                    }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TokopediaGreen)) {
                        Icon(Icons.Filled.Store, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lihat Dagangan Lain dari Penjual ini")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Rate seller
            OutlinedButton(onClick = { showRateDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rate Penjual")
            }

            // Report
            TextButton(onClick = { showReportDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Flag, contentDescription = null, tint = SoldRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Laporkan", color = SoldRed)
            }
        }
    }

    // Report dialog
    if (showReportDialog) {
        AlertDialog(onDismissRequest = { showReportDialog = false },
            title = { Text("Laporkan Listing") },
            text = {
                Column {
                    OutlinedTextField(value = reportName, onValueChange = { reportName = it },
                        label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = reportDesc, onValueChange = { reportDesc = it },
                        label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.report(l.id, reportName, reportDesc)
                    showReportDialog = false
                }) { Text("Kirim") }
            },
            dismissButton = { TextButton(onClick = { showReportDialog = false }) { Text("Batal") } }
        )
    }

    // Rate dialog
    if (showRateDialog) {
        AlertDialog(onDismissRequest = { showRateDialog = false },
            title = { Text("Rate Penjual") },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        for (i in 1..5) {
                            IconButton(onClick = { rating = i }) {
                                Icon(
                                    if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = null,
                                    tint = if (i <= rating) Orange500 else TextSecondary
                                )
                            }
                        }
                    }
                    OutlinedTextField(value = testimoni, onValueChange = { testimoni = it },
                        label = { Text("Testimoni (opsional)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.submitRating(l.id, rating, testimoni)
                    showRateDialog = false
                }) { Text("Kirim") }
            },
            dismissButton = { TextButton(onClick = { showRateDialog = false }) { Text("Batal") } }
        )
    }
}
