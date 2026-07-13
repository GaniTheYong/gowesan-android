package com.gowesan.app.data.model

import com.google.gson.annotations.SerializedName

// ── Listing ──

data class ListingResponse(
    val listings: List<Listing> = emptyList(),
    val page: Int = 1,
    val pages: Int = 1,
    val total: Int = 0
)

data class ListingDetailResponse(val listing: Listing)

data class Listing(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val category: String = "",
    val condition: String = "",
    val location: String? = null,
    val price: String? = null,
    val status: String = "draft",
    @SerializedName("online_store_url") val onlineStoreUrl: String? = null,
    @SerializedName("primary_photo") val primaryPhoto: String? = null,
    val photos: List<String>? = null,
    val videos: List<ListingVideo>? = null,
    val owner: Owner? = null,
    @SerializedName("like_count") val likeCount: Int = 0,
    @SerializedName("dislike_count") val dislikeCount: Int = 0,
    @SerializedName("listing_fee") val listingFee: Int? = null,
    @SerializedName("payment_code") val paymentCode: Int? = null,
    @SerializedName("payment_confirmed") val paymentConfirmed: Boolean = false,
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("listing_days") val listingDays: Int? = null,
    @SerializedName("credits_deducted") val creditsDeducted: Int? = null,
    @SerializedName("expiry_date") val expiryDate: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("sold_at") val soldAt: String? = null,
    @SerializedName("avg_rating") val avgRating: Float? = null
)

data class ListingPhoto(
    val id: String = "",
    @SerializedName("photo_url") val photoUrl: String = "",
    @SerializedName("is_primary") val isPrimary: Boolean = false
)

data class ListingVideo(
    val id: String = "",
    @SerializedName("video_url") val videoUrl: String = ""
)

data class Owner(
    val id: String = "",
    val username: String = "",
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val phone: String? = null,
    val city: String? = null
)

// ── User / Auth ──

data class User(
    val id: String = "",
    val username: String = "",
    @SerializedName("display_name") val displayName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    val credits: Int = 0,
    @SerializedName("is_admin") val isAdmin: Boolean = false
)

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(
    val username: String,
    val password: String,
    @SerializedName("display_name") val displayName: String,
    val email: String? = null,
    val phone: String? = null
)
data class ProfileUpdateRequest(
    @SerializedName("display_name") val displayName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val bio: String? = null
)
data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)
data class ApiResponse(val success: Boolean = false, val error: String? = null, val message: String? = null)

// ── Article ──

data class ArticleResponse(val articles: List<Article> = emptyList())
data class ArticleDetailResponse(val article: Article)
data class Article(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    @SerializedName("image_1") val thumbnailUrl: String? = null,
    @SerializedName("image_2") val image2: String? = null,
    @SerializedName("image_3") val image3: String? = null,
    @SerializedName("youtube_embed") val youtubeEmbed: String? = null,
    val category: String = "",
    @SerializedName("author_name") val authorName: String? = null,
    @SerializedName("author_avatar") val authorAvatar: String? = null,
    val status: String = "draft",
    @SerializedName("like_count") val likeCount: Int = 0,
    @SerializedName("dislike_count") val dislikeCount: Int = 0,
    @SerializedName("comment_count") val commentCount: Int = 0,
    @SerializedName("view_count") val viewCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null,
    val comments: List<Comment>? = null
)

data class Comment(
    val id: String = "",
    val content: String = "",
    @SerializedName("author_name") val authorName: String = "",
    @SerializedName("author_avatar") val authorAvatar: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// ── Event ──

data class EventResponse(val events: List<Event> = emptyList())
data class EventDetailResponse(val event: Event)
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val location: String? = null,
    @SerializedName("event_date") val eventDate: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("organizer_name") val organizerName: String? = null,
    @SerializedName("participant_count") val participantCount: Int = 0,
    @SerializedName("max_participants") val maxParticipants: Int? = null,
    val status: String = "",
    @SerializedName("created_at") val createdAt: String? = null,
    val organizer: Owner? = null
)

// ── Community ──

data class CommunityResponse(val communities: List<Community> = emptyList())
data class CommunityDetailResponse(val community: Community)
data class Community(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val city: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("member_count") val memberCount: Int = 0,
    @SerializedName("is_member") val isMember: Boolean = false,
    @SerializedName("is_admin") val isAdmin: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null,
    val owner: Owner? = null
)

data class CitiesResponse(val cities: List<String> = emptyList())

// ── Like / Report ──

data class LikeRequest(val type: String = "like")
data class LikeResponse(val success: Boolean = false, val action: String = "")
data class ReportRequest(
    @SerializedName("reporter_name") val reporterName: String,
    val contact: String = "",
    val description: String
)
data class RateSellerRequest(
    val rating: Int,
    val testimoni: String = ""
)
data class SellerCountResponse(val count: Int = 0)

// ── Search ──

data class SearchResponse(
    val listings: List<Listing> = emptyList(),
    val articles: List<Article> = emptyList(),
    val events: List<Event> = emptyList(),
    val communities: List<Community> = emptyList()
)

// ── Invoice ──

data class InvoiceResponse(
    val success: Boolean = false,
    val invoice: InvoiceData? = null
)
data class InvoiceData(
    val listings: List<Listing> = emptyList(),
    val total: Int = 0,
    @SerializedName("fee_only") val feeOnly: Int = 0
)
data class BulkPublishRequest(@SerializedName("listing_ids") val listingIds: List<String>)
data class PublishCreditRequest(
    @SerializedName("listing_days") val listingDays: Int,
    @SerializedName("start_date") val startDate: String? = null
)

// ── Credit ──

data class CreditsResponse(val credits: Int = 0)
data class CreditTransaction(
    val id: String = "",
    val type: String = "",
    val amount: Int = 0,
    val description: String = "",
    @SerializedName("created_at") val createdAt: String? = null
)

// ── Place ──

data class Place(
    val id: String = "",
    @SerializedName("title") val name: String = "",
    val address: String? = null,
    val city: String? = null,
    val category: String? = null,
    val photos: List<String>? = null,
    @SerializedName("avg_rating") val rating: Float? = null,
    val description: String? = null
) {
    val thumbnailUrl: String? get() = photos?.firstOrNull()
}
data class PlaceResponse(val places: List<Place> = emptyList())
data class PlaceDetailResponse(val place: Place)
data class SaranRequest(
    val title: String,
    val description: String = "",
    val category: String = "umum"
)
