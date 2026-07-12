package com.gowesan.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gowesan.app.data.api.SessionManager
import com.gowesan.app.data.model.User
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.navigation.Screen
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: GowesanRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun checkLogin() {
        viewModelScope.launch {
            sessionManager.isLoggedIn.collect { loggedIn ->
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    try {
                        val r = repo.getMe()
                        if (r.isSuccessful) _user.value = r.body()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { repo.logout() } catch (_: Exception) {}
            sessionManager.logout()
            _user.value = null
            _isLoggedIn.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.checkLogin() }

    if (!isLoggedIn) {
        // Not logged in
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = TokopediaGreen) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Person, null, modifier = Modifier.size(72.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Silakan login untuk mengakses profile", color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigate(Screen.Login.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = TokopediaGreen)) {
                        Text("Login")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                        Text("Daftar")
                    }
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Profile", fontWeight = FontWeight.Bold, color = TokopediaGreen) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            actions = {
                IconButton(onClick = { showLogoutDialog = true }) {
                    Icon(Icons.Filled.Logout, "Logout", tint = SoldRed)
                }
            }
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Profile header
            Card(colors = CardDefaults.cardColors(containerColor = DarkCard),
                modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(modifier = Modifier.padding(16.dp).clickable { navController.navigate(Screen.EditProfile.route) },
                    verticalAlignment = Alignment.CenterVertically) {
                    if (user?.avatarUrl != null) {
                        AsyncImage(model = user!!.avatarUrl, contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Filled.Person, null, modifier = Modifier.size(64.dp), tint = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user?.displayName ?: user?.username ?: "-",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text(user?.email ?: "", color = TextSecondary, fontSize = 13.sp)
                        Text(user?.city ?: "", color = TextSecondary, fontSize = 13.sp)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = TextSecondary)
                }
            }

            // Credits
            Card(colors = CardDefaults.cardColors(containerColor = DarkCard),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Row(modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Orange500, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Credit", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text("${user?.credits ?: 0}", fontWeight = FontWeight.Bold,
                        color = TokopediaGreen, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Menu items
            MenuItem(Icons.Outlined.Store, "Dagangan Saya") {
                navController.navigate(Screen.Transaksi.route)
            }
            MenuItem(Icons.Outlined.Article, "Artikel Saya") {
                navController.navigate(Screen.Feed.route) // temp
            }
            MenuItem(Icons.Outlined.Event, "Event Saya") {
                navController.navigate(Screen.Feed.route) // temp
            }
            MenuItem(Icons.Outlined.Groups, "Komunitas Saya") {
                navController.navigate(Screen.Feed.route) // temp
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = Color(0xFF333333))
            MenuItem(Icons.Outlined.Edit, "Edit Profile") {
                navController.navigate(Screen.EditProfile.route)
            }
            MenuItem(Icons.Outlined.Lock, "Ganti Password") {
                // dialog or screen
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = Color(0xFF333333))
            MenuItem(Icons.Outlined.Info, "Tentang Gowesan") { }
            MenuItem(Icons.Outlined.Logout, "Logout", color = SoldRed) {
                showLogoutDialog = true
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Yakin ingin logout?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    showLogoutDialog = false
                }) { Text("Logout", color = SoldRed) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
fun MenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String,
             color: androidx.compose.ui.graphics.Color = TextPrimary, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = color)
    }
}
