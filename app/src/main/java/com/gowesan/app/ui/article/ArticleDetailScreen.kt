package com.gowesan.app.ui.article

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gowesan.app.data.model.Article
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.ui.components.gowesanFieldColors
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun load(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val r = repo.getArticleDetail(id)
                if (r.isSuccessful) _article.value = r.body()?.article
            } catch (_: Exception) {}
            _loading.value = false
        }
    }

    fun like(id: String) {
        viewModelScope.launch {
            try { repo.likeArticle(id); load(id) } catch (_: Exception) {}
        }
    }

    fun comment(id: String, content: String) {
        viewModelScope.launch {
            try { repo.commentArticle(id, content); load(id) } catch (_: Exception) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(navController: NavController, articleId: String,
                        viewModel: ArticleDetailViewModel = hiltViewModel()) {
    val article by viewModel.article.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(articleId) { viewModel.load(articleId) }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TokopediaGreen)
        }
        return
    }

    val a = article ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(a.title, maxLines = 1, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Kembali") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            if (a.thumbnailUrl != null) {
                AsyncImage(model = a.thumbnailUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp), contentScale = ContentScale.Crop)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(a.authorName ?: "", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ThumbUp, null, tint = LikeBlue, modifier = Modifier.size(16.dp))
                    Text(" ${a.likeCount}", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Filled.ChatBubbleOutline, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Text(" ${a.commentCount}", color = TextSecondary, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(a.content, color = TextPrimary, fontSize = 15.sp, lineHeight = 22.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Like button
                OutlinedButton(onClick = { viewModel.like(a.id) },
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.ThumbUp, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Suka")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Comments
                Text("Komentar", fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = commentText, onValueChange = { commentText = it },
                        placeholder = { Text("Tulis komentar...") }, modifier = Modifier.weight(1f),
                        colors = gowesanFieldColors, singleLine = true)
                    IconButton(onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.comment(a.id, commentText)
                            commentText = ""
                        }
                    }) {
                        Icon(Icons.Filled.Send, "Kirim", tint = TokopediaGreen)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                a.comments?.forEach { c ->
                    Card(colors = CardDefaults.cardColors(containerColor = DarkCard),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(c.authorName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            Text(c.content, color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
