package com.promohub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promohub.app.ui.theme.HubOrange

/** Логотип PromoHub: "Promo" + "Hub" в оранжевом блоке (узнаваемый стиль). */
@Composable
fun PromoHubLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 44.sp,
    promoColor: Color = Color.White
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Promo",
            color = promoColor,
            fontWeight = FontWeight.Black,
            fontSize = fontSize
        )
        Box(
            modifier = Modifier
                .padding(start = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(HubOrange)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "Hub",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = fontSize
            )
        }
    }
}
