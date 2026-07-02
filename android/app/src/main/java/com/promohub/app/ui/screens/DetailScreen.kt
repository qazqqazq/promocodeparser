package com.promohub.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promohub.app.data.local.PromoCodeEntity
import com.promohub.app.domain.viewmodel.PromoViewModel
import com.promohub.app.ui.components.getServiceColor
import com.promohub.app.ui.theme.PrimaryGreen
import com.promohub.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    promoCode: PromoCodeEntity,
    promoViewModel: PromoViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали промокода") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = promoCode.service,
                    color = getServiceColor(promoCode.service),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = { promoViewModel.toggleFavorite(promoCode.id) }) {
                    Icon(
                        imageVector = if (promoCode.isFavorited) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Избранное",
                        tint = if (promoCode.isFavorited) Color.Red else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = promoCode.title,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryGreen.copy(alpha = 0.1f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Промокод",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = promoCode.code,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PrimaryGreen
                        )
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Промокод", promoCode.code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Промокод скопирован", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Копировать",
                            tint = PrimaryGreen
                        )
                    }
                }
            }

            promoCode.discount?.let { discount ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Скидка: ",
                            fontSize = 16.sp
                        )
                        Text(
                            text = discount,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Описание",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = promoCode.description ?: "Описание отсутствует",
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Работает",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "👍 ${promoCode.worksCount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryGreen
                    )
                }
                Column {
                    Text(
                        text = "Не работает",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "👎 ${promoCode.notWorkingCount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFE53935)
                    )
                }
                Column {
                    Text(
                        text = "Истекает",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = promoCode.expiresAt?.take(10) ?: "Бессрочно",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Промокод", promoCode.code)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Промокод скопирован", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Копировать промокод", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Промокод сработал?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Помогите другим: если код не сработал у двух человек — он скроется у всех.",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        promoViewModel.votePromocode(promoCode.id, isWorking = true) { result ->
                            result.onSuccess {
                                Toast.makeText(context, "Спасибо! Голос учтён", Toast.LENGTH_SHORT).show()
                            }
                            result.onFailure {
                                Toast.makeText(context, "Не удалось отправить голос", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    enabled = promoCode.userVote != true
                ) {
                    Text(if (promoCode.userVote == true) "👍 Ваш голос" else "👍 Работает", fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        promoViewModel.votePromocode(promoCode.id, isWorking = false) { result ->
                            result.onSuccess {
                                if (it.removed) {
                                    Toast.makeText(context, "Промокод скрыт: его отметили нерабочим", Toast.LENGTH_LONG).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "Спасибо! Голос учтён", Toast.LENGTH_SHORT).show()
                                }
                            }
                            result.onFailure {
                                Toast.makeText(context, "Не удалось отправить голос", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    enabled = promoCode.userVote != false
                ) {
                    Text(if (promoCode.userVote == false) "👎 Ваш голос" else "👎 Не работает", fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
