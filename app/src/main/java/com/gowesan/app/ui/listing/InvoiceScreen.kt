package com.gowesan.app.ui.listing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.gowesan.app.data.model.Listing
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun load(batchId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Load my listings and filter by batch
                val r = repo.getMyListings()
                if (r.isSuccessful) {
                    val all = r.body()?.listings ?: emptyList()
                    _listings.value = all.filter { it.status == "waiting_payment" }
                }
            } catch (_: Exception) {}
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(navController: NavController, batchId: String,
                  viewModel: InvoiceViewModel = hiltViewModel()) {
    val listings by viewModel.listings.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(batchId) { viewModel.load(batchId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Invoice", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TokopediaGreen)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                Card(colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rincian Pembayaran", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        listings.forEach { l ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(l.title, color = TextPrimary, modifier = Modifier.weight(1f))
                                Text("Rp ${(l.listingFee ?: 0)}", color = TextSecondary)
                            }
                        }
                        HorizontalDivider(color = Color(0xFF333333))
                        val total = listings.sumOf { (it.listingFee ?: 0) + (it.paymentCode ?: 0) }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("Rp $total", fontWeight = FontWeight.Bold, color = PriceGreen, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Transfer ke rekening yang tertera, lalu upload bukti bayar di Dashboard Web.",
                    color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TokopediaGreen)) {
                    Text("Selesai")
                }
            }
        }
    }
}
