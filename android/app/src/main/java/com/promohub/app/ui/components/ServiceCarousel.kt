package com.promohub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promohub.app.ui.theme.PrimaryGreen
import com.promohub.app.ui.theme.TextSecondary

val SERVICES = listOf(
    "Самокат", "Яндекс Еда", "Яндекс Лавка", "Delivery Club", "ВкусВилл",
    "СберМаркет", "Купер", "Пятёрочка Доставка", "KFC", "Папа Джонс",
    "Domino's Pizza", "FARFOR"
)

/**
 * Горизонтальная карусель сервисов: проматывается пальцем, выбор фильтрует список.
 */
@Composable
fun ServiceCarousel(
    selectedService: String?,
    onServiceSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            CarouselItem(
                label = "Все",
                service = null,
                selected = selectedService == null,
                onClick = { onServiceSelected(null) }
            )
        }
        items(SERVICES) { service ->
            CarouselItem(
                label = service,
                service = service,
                selected = selectedService == service,
                onClick = { onServiceSelected(service) }
            )
        }
    }
}

@Composable
private fun CarouselItem(
    label: String,
    service: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .then(
                    if (selected) Modifier.border(2.dp, Color.White, RoundedCornerShape(18.dp))
                    else Modifier
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (service != null) {
                ServiceIcon(service, size = 48.dp)
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Все", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (selected) Color.White else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
