package com.promohub.app.data.repository

import com.promohub.app.data.local.PromoCodeDao
import com.promohub.app.data.local.PromoCodeEntity
import com.promohub.app.data.local.TokenManager
import com.promohub.app.data.remote.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PromoRepository(
    private val api: ApiService,
    private val dao: PromoCodeDao,
    private val tokenManager: TokenManager
) {
    val token: Flow<String?> = tokenManager.token

    suspend fun login(username: String, password: String): Result<TokenResponse> {
        return try {
            val response = api.login(username, password)
            if (response.isSuccessful) {
                val body = response.body()!!
                // Сразу подставляем токен в исходящие запросы
                RetrofitClient.authToken = body.accessToken
                val userResponse = api.getMe().body()
                tokenManager.saveAuthData(
                    body.accessToken,
                    userResponse?.username ?: username,
                    userResponse?.email ?: ""
                )
                Result.success(body)
            } else {
                Result.failure(Exception("Ошибка авторизации"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<UserResponse> {
        return try {
            val response = api.register(RegisterRequest(username, email, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка регистрации"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        RetrofitClient.authToken = null
        tokenManager.clearAuthData()
    }

    fun getPromoCodes(): Flow<List<PromoCodeEntity>> = dao.getAllPromoCodes()

    fun getPromoCodesByService(service: String): Flow<List<PromoCodeEntity>> =
        dao.getPromoCodesByService(service)

    fun searchPromoCodes(query: String): Flow<List<PromoCodeEntity>> =
        dao.searchPromoCodes(query)

    suspend fun refreshPromoCodes(): Result<List<PromoCodeEntity>> {
        return try {
            val token = tokenManager.token.first()
            if (token == null) {
                return Result.failure(Exception("Необходима авторизация"))
            }
            val response = api.getPromocodes()
            if (response.isSuccessful) {
                val entities = response.body()!!.map { it.toEntity() }
                dao.clearCache()
                dao.insertAll(entities)
                Result.success(entities)
            } else {
                Result.failure(Exception("Ошибка загрузки данных"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshPromoCodesByService(service: String): Result<List<PromoCodeEntity>> {
        return try {
            val response = api.getPromocodes(service = service)
            if (response.isSuccessful) {
                val entities = response.body()!!.map { it.toEntity() }
                dao.clearCache()
                dao.insertAll(entities)
                Result.success(entities)
            } else {
                Result.failure(Exception("Ошибка загрузки данных"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchOnline(query: String): Result<List<PromoCodeEntity>> {
        return try {
            val response = api.getPromocodes(search = query)
            if (response.isSuccessful) {
                val entities = response.body()!!.map { it.toEntity() }
                Result.success(entities)
            } else {
                Result.failure(Exception("Ошибка поиска"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(promocodeId: Int): Result<Boolean> {
        return try {
            val response = api.toggleFavorite(promocodeId)
            if (response.isSuccessful) {
                Result.success(response.body()!!.isFavorited)
            } else {
                Result.failure(Exception("Ошибка"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavorites(): Result<List<PromoCodeEntity>> {
        return try {
            val response = api.getFavorites()
            if (response.isSuccessful) {
                val ids = response.body()!!.map { it.promocodeId }
                val entities = ids.mapNotNull { dao.getPromoCodeById(it) }
                Result.success(entities)
            } else {
                Result.failure(Exception("Ошибка загрузки избранного"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshFromSources(): Result<RefreshResponse> {
        return try {
            val response = api.refreshFromSources()
            if (response.isSuccessful) {
                // После парсинга перечитываем список из БД
                refreshPromoCodes()
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка обновления"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun votePromocode(id: Int, isWorking: Boolean): Result<VoteResponse> {
        return try {
            val response = api.votePromocode(id, VoteRequest(isWorking))
            if (response.isSuccessful) {
                val body = response.body()!!
                // Обновляем локальный кэш, чтобы UI сразу отразил голос
                dao.getPromoCodeById(id)?.let { entity ->
                    if (body.removed) {
                        dao.delete(entity)
                    } else {
                        dao.update(
                            entity.copy(
                                worksCount = body.worksCount,
                                notWorkingCount = body.notWorkingCount,
                                userVote = isWorking
                            )
                        )
                    }
                }
                Result.success(body)
            } else {
                Result.failure(Exception("Ошибка голосования"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(): Result<List<PromoCodeEntity>> {
        return try {
            val response = api.getHistory()
            if (response.isSuccessful) {
                val ids = response.body()!!.map { it.promocodeId }
                val entities = ids.mapNotNull { dao.getPromoCodeById(it) }
                Result.success(entities)
            } else {
                Result.failure(Exception("Ошибка загрузки истории"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearHistory(): Result<Unit> {
        return try {
            val response = api.clearHistory()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun PromoCodeResponse.toEntity() = PromoCodeEntity(
        id = id,
        code = code,
        title = title,
        description = description,
        service = service,
        discount = discount,
        expiresAt = expiresAt,
        isActive = isActive,
        rating = rating,
        usageCount = usageCount,
        sourceUrl = sourceUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorited = isFavorited,
        worksCount = worksCount,
        notWorkingCount = notWorkingCount,
        userVote = userVote,
        isCached = true
    )
}
