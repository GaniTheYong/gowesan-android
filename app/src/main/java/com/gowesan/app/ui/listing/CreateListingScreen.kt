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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.ui.components.gowesanFieldColors
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateListingViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    fun save(title: String, description: String, category: String, condition: String,
             location: String, price: String, onlineStoreUrl: String, onSuccess: () -> Unit,
             onError: (String) -> Unit) {
        viewModelScope.launch {
            _saving.value = true
            try {
                val data = mutableMapOf<String, Any>(
                    "title" to title, "description" to description,
                    "category" to category, "condition" to condition,
                    "location" to location, "online_store_url" to onlineStoreUrl
                )
                if (price.isNotBlank()) data["price"] = price
                val r = repo.createListing(data)
                if (r.isSuccessful) onSuccess() else onError("Gagal: ${r.code()}")
            } catch (e: Exception) { onError(e.localizedMessage ?: "Error") }
            _saving.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(navController: NavController, viewModel: CreateListingViewModel = hiltViewModel()) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var onlineStoreUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Lainnya") }
    var condition by remember { mutableStateOf("Bekas") }
    var expandedCat by remember { mutableStateOf(false) }
    var expandedCond by remember { mutableStateOf(false) }
    val saving by viewModel.saving.collectAsState()

    val categories = listOf("Sepeda", "Sparepart", "Aksesoris", "Jersey", "Lainnya")
    val conditions = listOf("Baru", "Bekas")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Pasang Dagangan", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            OutlinedTextField(value = title, onValueChange = { title = it },
                label = { Text("Judul *") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors)

            OutlinedTextField(value = desc, onValueChange = { desc = it },
                label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(),
                minLines = 4, colors = gowesanFieldColors)

            ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = it }) {
                OutlinedTextField(value = category, onValueChange = {}, readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = gowesanFieldColors)
                ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                    categories.forEach { c ->
                        DropdownMenuItem(text = { Text(c) }, onClick = { category = c; expandedCat = false })
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = expandedCond, onExpandedChange = { expandedCond = it }) {
                OutlinedTextField(value = condition, onValueChange = {}, readOnly = true,
                    label = { Text("Kondisi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCond) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = gowesanFieldColors)
                ExposedDropdownMenu(expanded = expandedCond, onDismissRequest = { expandedCond = false }) {
                    conditions.forEach { c ->
                        DropdownMenuItem(text = { Text(c) }, onClick = { condition = c; expandedCond = false })
                    }
                }
            }

            OutlinedTextField(value = price, onValueChange = { price = it },
                label = { Text("Harga (Rp)") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors)
            OutlinedTextField(value = location, onValueChange = { location = it },
                label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors)
            OutlinedTextField(value = onlineStoreUrl, onValueChange = { onlineStoreUrl = it },
                label = { Text("Link Toko Online (opsional)") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors)

            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    viewModel.save(title, desc, category, condition, location, price, onlineStoreUrl,
                        onSuccess = { navController.popBackStack() },
                        onError = { }
                    )
                },
                modifier = Modifier.fillMaxWidth(), enabled = !saving && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TokopediaGreen)
            ) {
                if (saving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkBg)
                else Text("Simpan Draft")
            }
        }
    }
}
