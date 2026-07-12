package com.gowesan.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gowesan.app.ui.components.gowesanFieldColors
import com.gowesan.app.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Daftar", fontWeight = FontWeight.Bold, color = TokopediaGreen) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Text("Buat Akun Gowesan", fontWeight = FontWeight.Bold,
                fontSize = 20.sp, color = TokopediaGreen)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = username, onValueChange = { username = it },
                label = { Text("Username *") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors, singleLine = true)
            OutlinedTextField(value = displayName, onValueChange = { displayName = it },
                label = { Text("Nama Tampilan *") }, modifier = Modifier.fillMaxWidth(),
                colors = gowesanFieldColors, singleLine = true)
            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = gowesanFieldColors, singleLine = true)
            OutlinedTextField(value = phone, onValueChange = { phone = it },
                label = { Text("WhatsApp") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = gowesanFieldColors, singleLine = true)
            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("Password *") }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = gowesanFieldColors, singleLine = true)

            if (error != null) {
                Text(error!!, color = SoldRed, fontSize = 13.sp)
            }

            Button(onClick = {
                if (username.isNotBlank() && password.isNotBlank() && displayName.isNotBlank())
                    viewModel.register(username, password, displayName,
                        email.ifBlank { null }, phone.ifBlank { null },
                        onSuccess = { navController.popBackStack() })
            }, modifier = Modifier.fillMaxWidth(), enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = TokopediaGreen)) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Daftar")
            }

            TextButton(onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()) {
                Text("Sudah punya akun? Login")
            }
        }
    }
}
