package com.promohub.app.data.remote

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class UserResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("created_at") val createdAt: String
)

data class PromoCodeResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("code") val code: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("service") val service: String,
    @SerializedName("discount") val discount: String?,
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("rating") val rating: Double,
    @SerializedName("usage_count") val usageCount: Int,
    @SerializedName("source_url") val sourceUrl: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("is_favorited") val isFavorited: Boolean,
    @SerializedName("works_count") val worksCount: Int = 0,
    @SerializedName("not_working_count") val notWorkingCount: Int = 0,
    @SerializedName("user_vote") val userVote: Boolean? = null
)

data class FavoriteResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("promocode_id") val promocodeId: Int,
    @SerializedName("created_at") val createdAt: String
)

data class HistoryResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("promocode_id") val promocodeId: Int,
    @SerializedName("viewed_at") val viewedAt: String
)

data class ToggleFavoriteResponse(
    @SerializedName("detail") val detail: String,
    @SerializedName("is_favorited") val isFavorited: Boolean
)

data class VoteRequest(
    @SerializedName("is_working") val isWorking: Boolean
)

data class VoteResponse(
    @SerializedName("detail") val detail: String,
    @SerializedName("works_count") val worksCount: Int,
    @SerializedName("not_working_count") val notWorkingCount: Int,
    @SerializedName("removed") val removed: Boolean
)

data class RefreshResponse(
    @SerializedName("detail") val detail: String,
    @SerializedName("found") val found: Int,
    @SerializedName("added") val added: Int
)
