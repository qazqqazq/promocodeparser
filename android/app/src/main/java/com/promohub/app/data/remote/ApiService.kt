package com.promohub.app.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserResponse>

    @GET("api/promocodes/")
    suspend fun getPromocodes(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50,
        @Query("service") service: String? = null,
        @Query("search") search: String? = null
    ): Response<List<PromoCodeResponse>>

    @GET("api/promocodes/{id}")
    suspend fun getPromocode(@Path("id") id: Int): Response<PromoCodeResponse>

    @POST("api/promocodes/refresh")
    suspend fun refreshFromSources(): Response<RefreshResponse>

    @POST("api/promocodes/{id}/vote")
    suspend fun votePromocode(
        @Path("id") id: Int,
        @Body request: VoteRequest
    ): Response<VoteResponse>

    @POST("api/promocodes/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") id: Int): Response<ToggleFavoriteResponse>

    @GET("api/promocodes/favorites/list")
    suspend fun getFavorites(): Response<List<FavoriteResponse>>

    @GET("api/promocodes/history/list")
    suspend fun getHistory(): Response<List<HistoryResponse>>

    @DELETE("api/promocodes/history/clear")
    suspend fun clearHistory(): Response<Map<String, String>>
}
