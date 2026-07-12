package com.gowesan.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gowesan.app.ui.auth.LoginScreen
import com.gowesan.app.ui.auth.RegisterScreen
import com.gowesan.app.ui.chat.ChatScreen
import com.gowesan.app.ui.feed.FeedScreen
import com.gowesan.app.ui.home.HomeScreen
import com.gowesan.app.ui.transaksi.TransaksiScreen
import com.gowesan.app.ui.profile.ProfileScreen
import com.gowesan.app.ui.listing.ListingDetailScreen
import com.gowesan.app.ui.listing.CreateListingScreen
import com.gowesan.app.ui.listing.InvoiceScreen
import com.gowesan.app.ui.listing.SellerListingsScreen
import com.gowesan.app.ui.event.EventDetailScreen
import com.gowesan.app.ui.article.ArticleDetailScreen
import com.gowesan.app.ui.community.CommunityDetailScreen
import com.gowesan.app.ui.profile.EditProfileScreen
import com.gowesan.app.ui.home.SearchScreen
import com.gowesan.app.ui.browse.PlacesBrowseScreen
import com.gowesan.app.ui.browse.ArticlesBrowseScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GowesanNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes where bottom bar should be hidden
    val bottomBarRoutes = bottomNavItems.map { it.screen.route }
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Bottom tabs
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Feed.route) { FeedScreen(navController) }
            composable(Screen.Transaksi.route) { TransaksiScreen(navController) }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Profile.route) { ProfileScreen(navController) }

            // Auth
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable(Screen.Register.route) { RegisterScreen(navController) }

            // Listing
            composable(
                Screen.ListingDetail.route,
                arguments = listOf(navArgument("listingId") { type = NavType.StringType })
            ) { backStackEntry ->
                ListingDetailScreen(
                    navController = navController,
                    listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                )
            }
            composable(Screen.CreateListing.route) { CreateListingScreen(navController) }

            // Invoice
            composable(
                Screen.Invoice.route,
                arguments = listOf(navArgument("batchId") { type = NavType.StringType })
            ) { backStackEntry ->
                InvoiceScreen(
                    navController = navController,
                    batchId = backStackEntry.arguments?.getString("batchId") ?: ""
                )
            }

            // Seller listings
            composable(
                Screen.SellerListings.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                SellerListingsScreen(
                    navController = navController,
                    userId = backStackEntry.arguments?.getString("userId") ?: ""
                )
            }

            // Article
            composable(
                Screen.ArticleDetail.route,
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { backStackEntry ->
                ArticleDetailScreen(
                    navController = navController,
                    articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                )
            }

            // Event
            composable(
                Screen.EventDetail.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                EventDetailScreen(
                    navController = navController,
                    eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                )
            }

            // Community
            composable(
                Screen.CommunityDetail.route,
                arguments = listOf(navArgument("communityId") { type = NavType.StringType })
            ) { backStackEntry ->
                CommunityDetailScreen(
                    navController = navController,
                    communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                )
            }

            // Profile
            composable(Screen.EditProfile.route) { EditProfileScreen(navController) }

            // Search
            composable(Screen.Search.route) { SearchScreen(navController) }

            // Browse all
            composable(Screen.Listings.route) { HomeScreen(navController) }
            composable(Screen.Places.route) { PlacesBrowseScreen(navController) }
            composable(Screen.Articles.route) { ArticlesBrowseScreen(navController) }

            // Auth dashboard (for non-logged-in transaction access)
            composable(Screen.AuthDashboard.route) {
                // Trigger auth and go to transaksi after
                navController.navigate(Screen.Login.route)
            }
        }
    }
}
