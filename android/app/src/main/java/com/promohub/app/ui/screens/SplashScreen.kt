package com.promohub.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promohub.app.ui.components.PromoHubLogo
import com.promohub.app.ui.theme.SplashBlack
import kotlinx.coroutines.delay

/** Стартовая заставка с анимацией появления логотипа. */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.7f,
        animationSpec = tween(700)
    )
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700)
    )

    LaunchedEffect(Unit) {
        started = true
        delay(1900)
        onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PromoHubLogo(
            modifier = Modifier
                .scale(scale)
                .alpha(alpha),
            fontSize = 52.sp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "промокоды доставки еды",
            color = Color.White.copy(alpha = 0.6f * alpha),
            fontSize = 14.sp
        )
    }
}
