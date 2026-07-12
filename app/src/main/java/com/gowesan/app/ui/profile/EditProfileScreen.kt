package com.gowesan.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.gowesan.app.data.api.SessionManager
import com.gowesan.app.data.model.ProfileUpdateRequest
import com.gowesan.app.data.model.User
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.ui.components.gowesanFieldColors
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repo: GowesanRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving
    private val _phoneChecked = MutableStateFlow<String?>(null)
    val phoneChecked: StateFlow<String?> = _phoneChecked

    fun load() {
        viewModelScope.launch {
            try {
                val r = repo.getMe()
                if (r.isSuccessful) _user.value = r.body()
            } catch (_: Exception) {}
        }
    }

    fun save(displayName: String, email: String, phone: String, city: String, bio: String,
             onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _saving.value = true
            try {
                val r = repo.updateProfile(ProfileUpdateRequest(
                    displayName = displayName.ifBlank { null },
                    email = email.ifBlank { null },
                    phone = phone.ifBlank { null },
                    city = city.ifBlank { null },
                    bio = bio.ifBlank { null }
                ))
                if (r.isSuccessful) onSuccess() else onError("Gagal: ${r.code()}")
            } catch (e: Exception) { onError(e.localizedMessage ?: "Error") }
            _saving.value = false
        }
    }

    fun checkPhone(phone: String) {
        viewModelScope.launch {
            try {
                val r = repo.checkPhone(phone)
                if (r.isSuccessful) _phoneChecked.value = "OK"
                else _phoneChecked.value = "Nomor sudah dipakai"
            } catch (_: Exception) { _phoneChecked.value = null }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, viewModel: EditProfileViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val phoneChecked by viewModel.phoneChecked.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(user) {
        user?.let {
            displayName = it.displayName ?: ""
            email = it.email ?: ""
            phone = it.phone ?: ""
            city = it.city ?: ""
            bio = it.bio ?: ""
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            OutlinedTextField(value = displayName, onValueChange = { displayName = it },
                label = { Text("Nama Tampilan") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors)
            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors)
            OutlinedTextField(value = phone, onValueChange = {
                phone = it
                if (it.length >= 10) viewModel.checkPhone(it)
                else phoneChecked?.let { /* reset */ }
            },
                label = { Text("Nomor WhatsApp") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors)
            if (phoneChecked != null) {
                Text(if (phoneChecked == "OK") "Nomor tersedia" else phoneChecked!!,
                    color = if (phoneChecked == "OK") TokopediaGreen else SoldRed, fontSize = 12.sp)
            }
            OutlinedTextField(value = city, onValueChange = { city = it },
                label = { Text("Kota") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors)
            OutlinedTextField(value = bio, onValueChange = { bio = it },
                label = { Text("Bio") }, modifier = Modifier.fillMaxWidth(),
                minLines = 3, colors = gowesanFieldColors)

            Button(onClick = {
                viewModel.save(displayName, email, phone, city, bio,
                    onSuccess = { navController.popBackStack() },
                    onError = { /* handle */ }
                )
            }, modifier = Modifier.fillMaxWidth(), enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = TokopediaGreen)) {
                if (saving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Simpan")
            }
        }
    }
}
