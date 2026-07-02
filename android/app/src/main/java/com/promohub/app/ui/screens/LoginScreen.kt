package com.promohub.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promohub.app.domain.viewmodel.AuthViewModel
import com.promohub.app.ui.components.LoadingSpinner
import com.promohub.app.ui.components.PromoHubLogo
import com.promohub.app.ui.theme.DarkSurface
import com.promohub.app.ui.theme.LoginGradientBottom
import com.promohub.app.ui.theme.LoginGradientMid
import com.promohub.app.ui.theme.LoginGradientTop
import com.promohub.app.ui.theme.PrimaryGreen

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val state by authViewModel.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }

    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardVisible = true }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(LoginGradientTop, LoginGradientMid, LoginGradientBottom)
                )
            )
    ) {
        AnimatedGreetingsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            PromoHubLogo(fontSize = 40.sp)
            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visible = cardVisible,
                enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(animationSpec = tween(600)) { full -> full / 3 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isLoginMode) "С возвращением 👋" else "Создать аккаунт",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isLoginMode) "Войдите, чтобы видеть промокоды"
                            else "Зарегистрируйтесь за пару секунд",
                            fontSize = 14.sp,
                            color = Color(0xFF8A8A8A)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Имя пользователя") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        if (!isLoginMode) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Пароль") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        state.error?.let { error ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (isLoginMode) authViewModel.login(username, password)
                                else authViewModel.register(username, email, password)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            enabled = !state.isLoading && username.isNotBlank() && password.isNotBlank()
                        ) {
                            if (state.isLoading) {
                                LoadingSpinner(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isLoginMode) "Войти" else "Зарегистрироваться",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(onClick = {
                            isLoginMode = !isLoginMode
                            authViewModel.clearError()
                        }) {
                            Text(
                                text = if (isLoginMode) "Нет аккаунта? Зарегистрироваться"
                                else "Уже есть аккаунт? Войти",
                                fontSize = 14.sp,
                                color = PrimaryGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** Фон с плавающими приветствиями на разных языках. */
@Composable
private fun AnimatedGreetingsBackground() {
    val greetings = listOf(
        Triple("Привет", 0.04f, 0.06f),
        Triple("Hello", 0.58f, 0.10f),
        Triple("Hola", 0.30f, 0.20f),
        Triple("Bonjour", 0.08f, 0.34f),
        Triple("Ciao", 0.62f, 0.30f),
        Triple("你好", 0.40f, 0.46f),
        Triple("Olá", 0.06f, 0.58f),
        Triple("Hallo", 0.55f, 0.54f),
        Triple("Salam", 0.28f, 0.68f),
        Triple("Привіт", 0.60f, 0.74f),
        Triple("こんにちは", 0.05f, 0.82f),
        Triple("Hi", 0.45f, 0.88f)
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val mw = maxWidth
        val mh = maxHeight
        greetings.forEachIndexed { i, (word, xf, yf) ->
            FloatingWord(
                text = word,
                x = mw * xf,
                y = mh * yf,
                durationMs = 2600 + i * 240
            )
        }
    }
}

@Composable
private fun FloatingWord(text: String, x: Dp, y: Dp, durationMs: Int) {
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Text(
        text = text,
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .offset(x = x, y = y)
            .graphicsLayer {
                alpha = 0.05f + phase * 0.13f
                translationY = (phase - 0.5f) * 36f
            }
    )
}
