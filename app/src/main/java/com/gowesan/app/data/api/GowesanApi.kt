package com.gowesan.app.data.api

import com.gowesan.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface GowesanApi {

    // ── Auth ──
    @POST("auth/api/login")
    suspend fun login(@Body body: LoginRequest): Response<User>

    @POST("auth/api/register")
    suspend fun register(@Body body: RegisterRequest): Response<User>

    @POST("auth/api/google-login")
    suspend fun googleLogin(@Body body: Map<String, String>): Response<User>

    @POST("auth/api/logout")
    suspend fun logout(): Response<ApiResponse>

    @GET("auth/api/me")
    suspend fun getMe(): Response<User>

    @PUT("auth/api/update-profile")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): Response<User>

    @POST("auth/api/check-phone")
    suspend fun checkPhone(@Body body: Map<String, String>): Response<ApiResponse>

    @PUT("auth/api/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<ApiResponse>

    // Profile photo upload
    @Multipart
    @POST("auth/profile")
    suspend fun uploadAvatar(@Part photo: MultipartBody.Part): Response<ApiResponse>

    // ── Listings ──
    @GET("listings/api/listings")
    suspend fun getListings(
        @Query("page") page: Int = 1,
        @Query("category") category: String? = null,
        @Query("condition") condition: String? = null,
        @Query("location") location: String? = null,
        @Query("sort") sort: String? = null
    ): Response<ListingResponse>

    @GET("listings/api/listings/{id}")
    suspend fun getListingDetail(@Path("id") listingId: String): Response<ListingDetailResponse>

    @GET("listings/api/seller/{userId}/listing-count")
    suspend fun getSellerListingCount(@Path("userId") userId: String): Response<SellerCountResponse>

    @POST("listings/api/listings/{id}/like")
    suspend fun toggleLike(@Path("id") listingId: String, @Body body: LikeRequest): Response<LikeResponse>

    @POST("listings/api/listings/{id}/report")
    suspend fun reportListing(@Path("id") listingId: String, @Body body: ReportRequest): Response<ApiResponse>

    @POST("listings/api/listings/{id}/rate-seller")
    suspend fun rateSeller(@Path("id") listingId: String, @Body body: RateSellerRequest): Response<ApiResponse>

    @GET("listings/api/listings/my")
    suspend fun getMyListings(): Response<ListingResponse>

    @POST("listings/api/listings")
    suspend fun createListing(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @PUT("listings/api/listings/{id}")
    suspend fun updateListing(@Path("id") listingId: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @DELETE("listings/api/listings/{id}")
    suspend fun deleteListing(@Path("id") listingId: String): Response<ApiResponse>

    @POST("listings/api/listings/bulk-publish")
    suspend fun bulkPublish(@Body body: BulkPublishRequest): Response<InvoiceResponse>

    @POST("listings/{id}/publish-with-credit")
    suspend fun publishWithCredit(@Path("id") listingId: String, @Body body: PublishCreditRequest): Response<ApiResponse>

    @POST("listings/{id}/stop")
    suspend fun stopListing(@Path("id") listingId: String): Response<ApiResponse>

    @POST("listings/{id}/habis")
    suspend fun markHabis(@Path("id") listingId: String): Response<ApiResponse>

    @Multipart
    @POST("listings/{id}/invoice/{batchId}/upload-receipt")
    suspend fun uploadReceipt(
        @Path("id") listingId: String,
        @Path("batchId") batchId: String,
        @Part receipt: MultipartBody.Part
    ): Response<ApiResponse>

    // ── Articles ──
    @GET("articles/api/articles")
    suspend fun getArticles(@Query("page") page: Int = 1): Response<ArticleResponse>

    @GET("articles/api/articles/{id}")
    suspend fun getArticleDetail(@Path("id") articleId: String): Response<ArticleDetailResponse>

    @POST("articles/api/articles/{id}/like")
    suspend fun likeArticle(@Path("id") articleId: String, @Body body: LikeRequest): Response<LikeResponse>

    @POST("articles/api/articles/{id}/comment")
    suspend fun commentArticle(@Path("id") articleId: String, @Body body: Map<String, String>): Response<ApiResponse>

    @GET("articles/api/articles/my")
    suspend fun getMyArticles(): Response<ArticleResponse>

    // ── Events ──
    @GET("events/api/events")
    suspend fun getEvents(): Response<EventResponse>

    @GET("events/api/events/{id}")
    suspend fun getEventDetail(@Path("id") eventId: String): Response<EventDetailResponse>

    @GET("events/api/events/my")
    suspend fun getMyEvents(): Response<EventResponse>

    @GET("events/api/events/created")
    suspend fun getMyCreatedEvents(): Response<EventResponse>

    @POST("events/api/events/search")
    suspend fun searchEvents(@Body body: Map<String, String>): Response<EventResponse>

    @POST("events/{id}/rate")
    suspend fun rateEvent(@Path("id") eventId: String, @Body body: Map<String, Int>): Response<ApiResponse>

    // ── Communities ──
    @GET("communities/api/communities")
    suspend fun getCommunities(): Response<CommunityResponse>

    @GET("communities/api/communities/{id}")
    suspend fun getCommunityDetail(@Path("id") communityId: String): Response<CommunityDetailResponse>

    @GET("communities/api/communities/cities")
    suspend fun getCommunityCities(): Response<CitiesResponse>

    @GET("communities/api/communities/search")
    suspend fun searchCommunities(@Query("q") query: String): Response<CommunityResponse>

    @POST("communities/api/communities/{id}/join")
    suspend fun joinCommunity(@Path("id") communityId: String): Response<ApiResponse>

    @GET("communities/api/communities/my")
    suspend fun getMyCommunities(): Response<CommunityResponse>

    // ── Places ──
    @GET("places/api/places")
    suspend fun getPlaces(): Response<PlaceResponse>

    @GET("places/api/places/{id}")
    suspend fun getPlaceDetail(@Path("id") placeId: String): Response<PlaceDetailResponse>

    // ── Search ──
    @GET("api/search")
    suspend fun search(@Query("q") query: String): Response<SearchResponse>

    // ── Credits ──
    @GET("auth/credits")
    suspend fun getCredits(): Response<CreditsResponse>

    // ── Saran ──
    @POST("api/saran")
    suspend fun submitSaran(@Body body: SaranRequest): Response<ApiResponse>
}
