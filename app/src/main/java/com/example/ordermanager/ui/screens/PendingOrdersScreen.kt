package com.example.ordermanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ordermanager.data.local.entity.PedidoEntity
import com.example.ordermanager.ui.components.EmptyState
import com.example.ordermanager.ui.components.OrderCard
import com.example.ordermanager.ui.theme.*
import com.example.ordermanager.ui.viewmodel.OrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class DisplayItem(
    val order: PedidoEntity,
    val isNew: Boolean = false,
    val isRemoving: Boolean = false
)

@Composable
fun PendingOrdersScreen(
    orderViewModel: OrderViewModel
) {
    val uiState by orderViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val displayItems = remember { mutableStateListOf<DisplayItem>() }

    LaunchedEffect(uiState.orders) {
        val viewModelIds = uiState.orders.map { it.id }.toSet()
        displayItems.removeAll { it.order.id !in viewModelIds && !it.isRemoving }
        uiState.orders.forEach { order ->
            val existing = displayItems.find { it.order.id == order.id }
            if (existing == null) {
                displayItems.add(DisplayItem(order, isNew = true))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${uiState.orders.size} pedido(s) en espera",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = Spacing.lg,
                vertical = Spacing.sm
            )
        )

        if (displayItems.isEmpty()) {
            EmptyState(
                title = "¡Todos los pedidos están al día!",
                subtitle = "Los nuevos pedidos aparecerán aquí automáticamente."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = Spacing.sm)
            ) {
                items(
                    items = displayItems.toList(),
                    key = { it.order.id }
                ) { item ->
                    AnimatedVisibility(
                        visible = !item.isRemoving,
                        enter = fadeIn(animationSpec = tween(400)) +
                                slideInVertically(animationSpec = tween(400)),
                        exit = slideOutHorizontally(animationSpec = tween(400)) { it } +
                                fadeOut(animationSpec = tween(400))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = Spacing.lg)
                                .padding(bottom = Spacing.md)
                        ) {
                            HighlightWrapper(
                                isNew = item.isNew,
                                onHighlightComplete = {
                                    val idx = displayItems.indexOfFirst { it.order.id == item.order.id }
                                    if (idx >= 0 && !displayItems[idx].isRemoving) {
                                        displayItems[idx] = displayItems[idx].copy(isNew = false)
                                    }
                                }
                            ) {
                                OrderCard(
                                    order = item.order,
                                    onMarcarEnviado = { id ->
                                        val idx = displayItems.indexOfFirst { it.order.id == id }
                                        if (idx >= 0) {
                                            displayItems[idx] = displayItems[idx].copy(isRemoving = true)
                                            scope.launch {
                                                delay(400)
                                                displayItems.removeAll { it.order.id == id }
                                                orderViewModel.marcarEnviado(id)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightWrapper(
    isNew: Boolean,
    onHighlightComplete: () -> Unit,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(if (isNew) 0.15f else 0f) }

    LaunchedEffect(isNew) {
        if (isNew) {
            alpha.snapTo(0.15f)
            alpha.animateTo(0f, animationSpec = tween(1500))
            onHighlightComplete()
        }
    }

    Box {
        content()
        if (alpha.value > 0.001f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(Shapes.cardLarge)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha.value))
            )
        }
    }
}
