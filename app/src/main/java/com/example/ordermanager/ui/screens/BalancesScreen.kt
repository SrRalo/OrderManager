package com.example.ordermanager.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ordermanager.ui.components.EmptyState

@Composable
fun BalancesScreen() {
    EmptyState(
        title = "Balances",
        subtitle = "Próximamente",
        modifier = Modifier.fillMaxSize()
    )
}
