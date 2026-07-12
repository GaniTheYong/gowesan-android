package com.gowesan.app.ui.community

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gowesan.app.data.model.Community
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    private val _community = MutableStateFlow<Community?>(null)
    val community: StateFlow<Community?> = _community
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun load(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val r = repo.getCommunityDetail(id)
                if (r.isSuccessful) _community.value = r.body()?.community
            } catch (_: Exception) {}
            _loading.value = false
        }
    }

    fun join(id: String) {
        viewModelScope.launch {
            try { repo.joinCommunity(id); load(id) } catch (_: Exception) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(navController: NavController, communityId: String,
                          viewModel: CommunityDetailViewModel = hiltViewModel()) {
    val community by viewModel.community.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(communityId) { viewModel.load(communityId) }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TokopediaGreen)
        }
        return
    }

    val c = community ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(c.name, maxLines = 1, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            if (c.thumbnailUrl != null) {
                AsyncImage(model = c.thumbnailUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp), contentScale = ContentScale.Crop)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(c.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Card(colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.People, null, tint = TokopediaGreen, modifier = Modifier.size(20.dp))
                            Text("${c.memberCount}", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Anggota", fontSize = 11.sp, color = TextSecondary)
                        }
                        if (c.city != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.LocationOn, null, tint = SoldRed, modifier = Modifier.size(20.dp))
                                Text(c.city!!, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Kota", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                if (!c.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tentang Komunitas", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(c.description!!, color = TextSecondary, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.join(c.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (c.isMember) TextSecondary else TokopediaGreen
                    )) {
                    Icon(if (c.isMember) Icons.Filled.Check else Icons.Filled.GroupAdd,
                        null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (c.isMember) "Sudah Bergabung" else "Gabung Komunitas")
                }
            }
        }
    }
}
