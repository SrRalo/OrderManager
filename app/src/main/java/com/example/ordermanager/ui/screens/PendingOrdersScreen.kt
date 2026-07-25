package com.example.ordermanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ordermanager.ui.components.EmptyState
import com.example.ordermanager.ui.components.OrderCard
import com.example.ordermanager.ui.theme.*
import com.example.ordermanager.ui.viewmodel.OrderViewModel

@Composable
fun PendingOrdersScreen(
    orderViewModel: OrderViewModel
) {
    val uiState by orderViewModel.uiState.collectAsStateWithLifecycle()
    val orders = uiState.orders
    val pendingConfirmOrders = uiState.pendingConfirmOrders
    val confirmTimers = uiState.confirmTimers
    val listState = rememberLazyListState()

    // Auto-scroll solo cuando llega un pedido nuevo (no al cancelar uno)
    LaunchedEffect(uiState.newOrderAlert) {
        if (uiState.newOrderAlert != null && orders.isNotEmpty()) {
            listState.animateScrollToItem(orders.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (orders.isEmpty() && pendingConfirmOrders.isEmpty()) {
            EmptyState(
                title = "¡Todos los pedidos están al día!",
                subtitle = "Los nuevos pedidos aparecerán aquí automáticamente."
            )
        } else {
            val allItems = remember(orders, pendingConfirmOrders, confirmTimers) {
                val combined = orders.map { it to false } +
                    pendingConfirmOrders.values.map { it to true }
                // Extra safety: ensure unique keys even if state has issues
                combined.distinctBy { it.first.id }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = Spacing.xl + 40.dp,
                    bottom = Spacing.sm
                )
            ) {
                items(
                    items = allItems,
                    key = { (order, _) -> order.id }
                ) { (order, isPending) ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = Spacing.lg)
                            .padding(bottom = Spacing.md)
                    ) {
                        OrderCard(
                            order = order,
                            isPendingConfirm = isPending,
                            confirmTimer = confirmTimers[order.id] ?: 0,
                            onMarcarEnviado = { orderViewModel.startSendConfirmation(it) },
                            onCancelSend = { orderViewModel.cancelSend(it) }
                        )
                    }
                }
            }
        }

        val totalCount = orders.size + pendingConfirmOrders.size
        if (totalCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = Spacing.md)
                    .shadow(6.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "$totalCount pedido(s) en espera",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        }
    }
}
