package com.promohub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.promohub.app.data.local.TokenManager
import com.promohub.app.data.remote.RetrofitClient
import com.promohub.app.ui.navigation.PromoHubNavHost
import com.promohub.app.ui.theme.PromoHubTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Адрес сервера вшит в RetrofitClient. Подгружаем только токен авторизации.
        runBlocking {
            RetrofitClient.authToken = TokenManager(applicationContext).token.first()
        }

        setContent {
            PromoHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PromoHubNavHost()
                }
            }
        }
    }
}
