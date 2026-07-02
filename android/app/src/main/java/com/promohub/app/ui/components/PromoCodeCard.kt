package com.promohub.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promohub.app.data.local.PromoCodeEntity
import com.promohub.app.ui.theme.*

@Composable
fun PromoCodeCard(
    promoCode: PromoCodeEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Мягкое появление карточки при прокрутке в зону видимости
    val appear = remember { MutableTransitionState(false) }
    appear.targetState = true

    // Плавное сжатие при нажатии
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(150)
    )

    AnimatedVisibility(
        visibleState = appear,
        modifier = modifier,
        enter = fadeIn(tween(320)) +
            slideInVertically(tween(320)) { it / 8 } +
            scaleIn(tween(320), initialScale = 0.96f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = interaction,
                    indication = LocalIndication.current,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ServiceIcon(promoCode.service, size = 34.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = promoCode.service,
                            color = getServiceColor(promoCode.service),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (promoCode.isFavorited) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Избранное",
                            tint = if (promoCode.isFavorited) Color(0xFFFF5C7A) else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = promoCode.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = promoCode.description ?: "",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    promoCode.discount?.let { discount ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryGreen.copy(alpha = 0.14f))
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = discount,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } ?: Spacer(modifier = Modifier.width(1.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👍 ${promoCode.worksCount}",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "👎 ${promoCode.notWorkingCount}",
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

fun getServiceColor(service: String): Color = when (service) {
    "Самокат" -> ServiceSamokat
    "Яндекс Еда" -> ServiceYandexEda
    "Яндекс Лавка" -> ServiceYandexLavka
    "Delivery Club" -> ServiceDeliveryClub
    "ВкусВилл" -> ServiceVkusVill
    "СберМаркет" -> ServiceSberMarket
    "Купер" -> ServiceKuper
    "Пятёрочка Доставка" -> ServicePyaterochka
    "KFC" -> ServiceKFC
    "Папа Джонс" -> ServicePapaJohns
    "Domino's Pizza" -> ServiceDominos
    "FARFOR" -> ServiceFarfor
    else -> TextSecondary
}
