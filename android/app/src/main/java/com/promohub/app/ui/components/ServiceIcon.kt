package com.promohub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Иконка сервиса: фирменная цветная «плитка» с монограммой.
 * Чтобы заменить на настоящий логотип — положи PNG в res/drawable и
 * отрисуй Image(painterResource(...)) для нужного сервиса вместо Text.
 */
@Composable
fun ServiceIcon(
    service: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val mono = serviceMonogram(service)
    val fontFactor = when (mono.length) {
        1 -> 0.46f
        2 -> 0.34f
        else -> 0.26f
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(getServiceColor(service)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mono,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * fontFactor).sp,
            maxLines = 1
        )
    }
}

fun serviceMonogram(service: String): String = when (service) {
    "Самокат" -> "С"
    "Яндекс Еда" -> "ЯЕ"
    "Яндекс Лавка" -> "ЯЛ"
    "Delivery Club" -> "DC"
    "ВкусВилл" -> "ВВ"
    "СберМаркет" -> "СМ"
    "Купер" -> "Ку"
    "Пятёрочка Доставка" -> "5"
    "KFC" -> "KFC"
    "Папа Джонс" -> "PJ"
    "Domino's Pizza" -> "D"
    "FARFOR" -> "F"
    else -> service.take(1).uppercase()
}
