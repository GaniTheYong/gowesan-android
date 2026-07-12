package com.gowesan.app.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gowesan.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Chat", fontWeight = FontWeight.Bold, color = TokopediaGreen) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Forum, null, modifier = Modifier.size(72.dp), tint = TokopediaGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chat via WhatsApp", fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Semua komunikasi penjual-pembeli", color = TextSecondary)
                Text("dilakukan melalui WhatsApp.", color = TextSecondary)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/?text=Halo Gowesan!"))
                    context.startActivity(intent)
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) {
                    Icon(Icons.Filled.Call, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buka WhatsApp", color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}
