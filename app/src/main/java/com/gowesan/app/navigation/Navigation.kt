package com.gowesan.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Feed : Screen("feed")
    object Listings : Screen("all_listings")
    object Places : Screen("all_places")
    object Articles : Screen("all_articles")
    object Transaksi : Screen("transaksi")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object ListingDetail : Screen("listing/{listingId}") {
        fun createRoute(id: String) = "listing/$id"
    }
    object CreateListing : Screen("create_listing")
    object EditListing : Screen("edit_listing/{listingId}") {
        fun createRoute(id: String) = "edit_listing/$id"
    }
    object Invoice : Screen("invoice/{batchId}") {
        fun createRoute(ids: String) = "invoice/$ids"
    }
    object SellerListings : Screen("seller/{userId}/listings") {
        fun createRoute(userId: String) = "seller/$userId/listings"
    }
    object ArticleDetail : Screen("article/{articleId}") {
        fun createRoute(id: String) = "article/$id"
    }
    object EventDetail : Screen("event/{eventId}") {
        fun createRoute(id: String) = "event/$id"
    }
    object CommunityDetail : Screen("community/{communityId}") {
        fun createRoute(id: String) = "community/$id"
    }
    object PlaceDetail : Screen("place/{placeId}") {
        fun createRoute(id: String) = "place/$id"
    }
    object Login : Screen("login")
    object Register : Screen("register")
    object EditProfile : Screen("edit_profile")
    object Search : Screen("search")
    object AuthDashboard : Screen("auth_dashboard")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Feed, "Feed", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
    BottomNavItem(Screen.Transaksi, "Transaksi", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    BottomNavItem(Screen.Chat, "Chat", Icons.Filled.Forum, Icons.Outlined.Forum),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)
