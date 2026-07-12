package com.gowesan.app.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gowesan.app.data.model.Article
import com.gowesan.app.data.model.Community
import com.gowesan.app.data.model.Event
import com.gowesan.app.data.repository.GowesanRepository
import com.gowesan.app.navigation.Screen
import com.gowesan.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(private val repo: GowesanRepository) : ViewModel() {
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events
    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    private var selectedTab = 0

    fun loadArticles() {
        viewModelScope.launch {
            try {
                val r = repo.getArticles()
                if (r.isSuccessful) _articles.value = r.body()?.articles ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                val r = repo.getEvents()
                if (r.isSuccessful) _events.value = r.body()?.events ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun loadCommunities() {
        viewModelScope.launch {
            try {
                val r = repo.getCommunities()
                if (r.isSuccessful) _communities.value = r.body()?.communities ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _loading.value = true
            loadArticles()
            loadEvents()
            loadCommunities()
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(navController: NavController, viewModel: FeedViewModel = hiltViewModel()) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Artikel", "Event", "Komunitas")
    val articles by viewModel.articles.collectAsState()
    val events by viewModel.events.collectAsState()
    val communities by viewModel.communities.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAll() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Feed", fontWeight = FontWeight.Bold, color = TokopediaGreen) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        TabRow(selectedTabIndex = tabIndex, containerColor = DarkSurface,
            contentColor = TokopediaGreen,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                    color = TokopediaGreen
                )
            }) {
            tabs.forEachIndexed { i, name ->
                Tab(selected = tabIndex == i, onClick = { tabIndex = i },
                    text = { Text(name) })
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TokopediaGreen)
            }
        } else {
            when (tabIndex) {
                0 -> LazyColumn {
                    items(articles) { a -> ArticleItem(a) { navController.navigate(Screen.ArticleDetail.createRoute(a.id)) } }
                }
                1 -> LazyColumn {
                    items(events) { e -> EventItem(e) { navController.navigate(Screen.EventDetail.createRoute(e.id)) } }
                }
                2 -> LazyColumn {
                    items(communities) { c -> CommunityItem(c) { navController.navigate(Screen.CommunityDetail.createRoute(c.id)) } }
                }
            }
        }
    }
}

@Composable
fun ArticleItem(article: Article, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp)) {
        AsyncImage(model = article.thumbnailUrl, contentDescription = null,
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(article.title, fontWeight = FontWeight.Bold, color = TextPrimary,
                maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
            Text(article.authorName ?: "", color = TextSecondary, fontSize = 12.sp)
            Row {
                Icon(Icons.Filled.ThumbUp, null, modifier = Modifier.size(12.dp), tint = TextSecondary)
                Text(" ${article.likeCount}", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Filled.ChatBubbleOutline, null, modifier = Modifier.size(12.dp), tint = TextSecondary)
                Text(" ${article.commentCount}", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp)) {
        AsyncImage(model = event.thumbnailUrl, contentDescription = null,
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(event.title, fontWeight = FontWeight.Bold, color = TextPrimary,
                maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
            Text(event.location ?: "", color = LikeBlue, fontSize = 12.sp)
            Text("${event.participantCount} peserta", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun CommunityItem(community: Community, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp)) {
        AsyncImage(model = community.thumbnailUrl, contentDescription = null,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(community.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
            Text(community.city ?: "", color = TextSecondary, fontSize = 12.sp)
            Text("${community.memberCount} anggota", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
