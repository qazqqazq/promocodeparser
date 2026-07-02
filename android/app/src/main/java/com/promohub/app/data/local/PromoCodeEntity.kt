package com.promohub.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promocodes")
data class PromoCodeEntity(
    @PrimaryKey val id: Int,
    val code: String,
    val title: String,
    val description: String?,
    val service: String,
    val discount: String?,
    val expiresAt: String?,
    val isActive: Boolean,
    val rating: Double,
    val usageCount: Int,
    val sourceUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val isFavorited: Boolean,
    val worksCount: Int = 0,
    val notWorkingCount: Int = 0,
    // Голос пользователя: true — работает, false — не работает, null — не голосовал
    val userVote: Boolean? = null,
    val isCached: Boolean = true
)
