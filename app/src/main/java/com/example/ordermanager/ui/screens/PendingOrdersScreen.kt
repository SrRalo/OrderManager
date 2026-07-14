package com.example.ordermanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${orders.size} pedido(s) en espera",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = Spacing.lg,
                vertical = Spacing.sm
            )
        )

        if (orders.isEmpty()) {
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
                    items = orders,
                    key = { it.id }
                ) { order ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = Spacing.lg)
                            .padding(bottom = Spacing.md)
                    ) {
                        OrderCard(
                            order = order,
                            onMarcarEnviado = { orderViewModel.marcarEnviado(it) }
                        )
                    }
                }
            }
        }
    }
}
