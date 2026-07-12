package com.gowesan.app.ui.event

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.gowesan.app.data.model.Event
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun load(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val r = repo.getEventDetail(id)
                if (r.isSuccessful) _event.value = r.body()?.event
            } catch (_: Exception) {}
            _loading.value = false
        }
    }

    fun rate(id: String, rating: Int) {
        viewModelScope.launch {
            try { repo.rateEvent(id, rating); load(id) } catch (_: Exception) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(navController: NavController, eventId: String,
                      viewModel: EventDetailViewModel = hiltViewModel()) {
    val event by viewModel.event.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current
    var showRateDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(5) }

    LaunchedEffect(eventId) { viewModel.load(eventId) }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TokopediaGreen)
        }
        return
    }

    val e = event ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(e.title, maxLines = 1, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            if (e.thumbnailUrl != null) {
                AsyncImage(model = e.thumbnailUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp), contentScale = ContentScale.Crop)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(e.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = LikeBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(e.eventDate ?: "TBA", color = LikeBlue, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = SoldRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(e.location ?: "TBA", color = TextSecondary, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.People, null, tint = TokopediaGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${e.participantCount} peserta", color = TextSecondary, fontSize = 13.sp)
                }

                if (!e.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Deskripsi", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(e.description!!, color = TextSecondary, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showRateDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500)) {
                    Icon(Icons.Filled.Star, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rate Event")
                }

                if (e.location != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        val uri = Uri.parse("geo:0,0?q=${e.location}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Map, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buka di Maps")
                    }
                }
            }
        }
    }

    if (showRateDialog) {
        AlertDialog(onDismissRequest = { showRateDialog = false },
            title = { Text("Rate Event") },
            text = {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                null, tint = if (i <= rating) Orange500 else TextSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rate(e.id, rating)
                    showRateDialog = false
                }) { Text("Kirim") }
            },
            dismissButton = { TextButton(onClick = { showRateDialog = false }) { Text("Batal") } }
        )
    }
}
