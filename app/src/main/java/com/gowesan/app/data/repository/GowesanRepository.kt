package com.gowesan.app.data.repository

import com.gowesan.app.data.api.GowesanApi
import com.gowesan.app.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GowesanRepository @Inject constructor(private val api: GowesanApi) {

    // ── Auth ──
    suspend fun login(username: String, password: String) = api.login(LoginRequest(username, password))
    suspend fun register(username: String, password: String, displayName: String, email: String?, phone: String?) =
        api.register(RegisterRequest(username, password, displayName, email, phone))
    suspend fun logout() = api.logout()
    suspend fun getMe() = api.getMe()
    suspend fun updateProfile(body: ProfileUpdateRequest) = api.updateProfile(body)
    suspend fun checkPhone(phone: String) = api.checkPhone(mapOf("phone" to phone))
    suspend fun changePassword(current: String, new: String) = api.changePassword(ChangePasswordRequest(current, new))
    suspend fun uploadAvatar(file: File): retrofit2.Response<ApiResponse> {
        val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("photo", file.name, reqBody)
        return api.uploadAvatar(part)
    }

    // ── Listings ──
    suspend fun getListings(page: Int = 1, category: String? = null, condition: String? = null,
                            location: String? = null, sort: String? = null) =
        api.getListings(page, category, condition, location, sort)

    suspend fun getListingDetail(id: String) = api.getListingDetail(id)
    suspend fun getSellerListingCount(userId: String) = api.getSellerListingCount(userId)
    suspend fun toggleLike(listingId: String, type: String = "like") = api.toggleLike(listingId, LikeRequest(type))
    suspend fun reportListing(listingId: String, name: String, contact: String, desc: String) =
        api.reportListing(listingId, ReportRequest(name, contact, desc))
    suspend fun rateSeller(listingId: String, rating: Int, testimoni: String = "") =
        api.rateSeller(listingId, RateSellerRequest(rating, testimoni))
    suspend fun getMyListings() = api.getMyListings()
    suspend fun createListing(data: Map<String, Any>) = api.createListing(data)
    suspend fun updateListing(id: String, data: Map<String, Any>) = api.updateListing(id, data)
    suspend fun deleteListing(id: String) = api.deleteListing(id)
    suspend fun bulkPublish(ids: List<String>) = api.bulkPublish(BulkPublishRequest(ids))
    suspend fun publishWithCredit(id: String, days: Int, startDate: String? = null) =
        api.publishWithCredit(id, PublishCreditRequest(days, startDate))
    suspend fun stopListing(id: String) = api.stopListing(id)
    suspend fun markHabis(id: String) = api.markHabis(id)

    // ── Articles ──
    suspend fun getArticles(page: Int = 1) = api.getArticles(page)
    suspend fun getArticleDetail(id: String) = api.getArticleDetail(id)
    suspend fun likeArticle(id: String, type: String = "like") = api.likeArticle(id, LikeRequest(type))
    suspend fun commentArticle(id: String, content: String) = api.commentArticle(id, mapOf("content" to content))
    suspend fun getMyArticles() = api.getMyArticles()

    // ── Events ──
    suspend fun getEvents() = api.getEvents()
    suspend fun getEventDetail(id: String) = api.getEventDetail(id)
    suspend fun getMyEvents() = api.getMyEvents()
    suspend fun getMyCreatedEvents() = api.getMyCreatedEvents()
    suspend fun searchEvents(query: String) = api.searchEvents(mapOf("q" to query))
    suspend fun rateEvent(id: String, rating: Int) = api.rateEvent(id, mapOf("rating" to rating))

    // ── Communities ──
    suspend fun getCommunities() = api.getCommunities()
    suspend fun getCommunityDetail(id: String) = api.getCommunityDetail(id)
    suspend fun getCommunityCities() = api.getCommunityCities()
    suspend fun searchCommunities(query: String) = api.searchCommunities(query)
    suspend fun joinCommunity(id: String) = api.joinCommunity(id)
    suspend fun getMyCommunities() = api.getMyCommunities()

    // ── Places ──
    suspend fun getPlaces() = api.getPlaces()
    suspend fun getPlaceDetail(id: String) = api.getPlaceDetail(id)

    // ── Search ──
    suspend fun search(query: String) = api.search(query)

    // ── Credits ──
    suspend fun getCredits() = api.getCredits()

    // ── Saran ──
    suspend fun submitSaran(body: SaranRequest) = api.submitSaran(body)
}
