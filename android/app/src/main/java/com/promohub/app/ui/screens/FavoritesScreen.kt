package com.promohub.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.promohub.app.domain.viewmodel.PromoViewModel
import com.promohub.app.ui.components.LoadingSpinner
import com.promohub.app.ui.components.PromoCodeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    promoViewModel: PromoViewModel,
    onPromoCodeClick: (Int) -> Unit
) {
    val state by promoViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        promoViewModel.getFavorites()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Избранное") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingSpinner()
            }
        } else if (state.promocodes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет избранных промокодов",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(state.promocodes, key = { it.id }) { promoCode ->
                    PromoCodeCard(
                        promoCode = promoCode,
                        onClick = { onPromoCodeClick(promoCode.id) },
                        onFavoriteClick = { promoViewModel.toggleFavorite(promoCode.id) }
                    )
                }
            }
        }
    }
}
