package com.promohub.app.data.remote

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Постоянный адрес сервера, вшитый в приложение (ngrok-туннель до ПК).
    // Менять не нужно — он статический.
    const val DEFAULT_BASE_URL = "https://usual-splashing-slug.ngrok-free.dev/"

    // Базовый адрес сервера. Хранится отдельно, чтобы менять его на лету
    // без пересборки APK — запрос переписывается интерцептором.
    @Volatile
    private var baseHost: HttpUrl = DEFAULT_BASE_URL.toHttpUrl()

    @Volatile
    var authToken: String? = null

    /** Меняет адрес сервера. Возвращает true, если URL корректный. */
    fun setBaseUrl(url: String): Boolean {
        val normalized = url.trim().let { if (it.endsWith("/")) it else "$it/" }
        val parsed = normalized.toHttpUrlOrNull() ?: return false
        baseHost = parsed
        return true
    }

    fun currentBaseUrl(): String = baseHost.toString()

    // Подставляет актуальный хост/схему/порт и токен авторизации в каждый запрос.
    private val networkInterceptor = Interceptor { chain ->
        val original = chain.request()
        val base = baseHost
        val newUrl = original.url.newBuilder()
            .scheme(base.scheme)
            .host(base.host)
            .port(base.port)
            .build()
        val builder = original.newBuilder().url(newUrl)
        // Чтобы ngrok не отдавал страницу-предупреждение вместо ответа API
        builder.addHeader("ngrok-skip-browser-warning", "true")
        authToken?.let { builder.addHeader("Authorization", "Bearer $it") }
        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(networkInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        // Заглушка: реальные схема/хост/порт подставляются networkInterceptor'ом.
        .baseUrl(DEFAULT_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}
