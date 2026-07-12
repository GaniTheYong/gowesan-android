package com.gowesan.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.gowesan.app.data.api.SessionManager
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.navigation.Screen
import com.gowesan.app.ui.components.gowesanFieldColors
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: GowesanRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true; _error.value = null
            try {
                val r = repo.login(username, password)
                if (r.isSuccessful && r.body() != null) {
                    val u = r.body()!!
                    session.saveLogin(u.id, u.username, u.displayName)
                    onSuccess()
                } else {
                    _error.value = "Login gagal. Periksa username/password."
                }
            } catch (e: Exception) { _error.value = "Koneksi error" }
            _loading.value = false
        }
    }

    fun register(username: String, password: String, displayName: String, email: String?, phone: String?,
                 onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true; _error.value = null
            try {
                val r = repo.register(username, password, displayName, email, phone)
                if (r.isSuccessful && r.body() != null) {
                    val u = r.body()!!
                    session.saveLogin(u.id, u.username, u.displayName)
                    onSuccess()
                } else {
                    _error.value = "Registrasi gagal. Username mungkin sudah dipakai."
                }
            } catch (e: Exception) { _error.value = "Koneksi error" }
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Login", fontWeight = FontWeight.Bold, color = TokopediaGreen) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.Center) {

            Text("Selamat Datang di Gowesan", fontWeight = FontWeight.Bold,
                fontSize = 20.sp, color = TokopediaGreen)
            Text("Komunitas Pesepeda Indonesia", color = TextSecondary)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(value = username, onValueChange = { username = it },
                label = { Text("Username") }, modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                colors = gowesanFieldColors, singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = gowesanFieldColors, singleLine = true)

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error!!, color = SoldRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (username.isNotBlank() && password.isNotBlank())
                    viewModel.login(username, password, onSuccess = { navController.popBackStack() })
            }, modifier = Modifier.fillMaxWidth(), enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = TokopediaGreen)) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate(Screen.Register.route) },
                modifier = Modifier.fillMaxWidth()) {
                Text("Belum punya akun? Daftar")
            }
        }
    }
}
