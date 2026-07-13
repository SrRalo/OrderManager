package com.example.ordermanager.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ordermanager.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.twoXl),
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (title != null || navigationIcon != null) {
                TopAppBar(
                    title = { Text(title ?: "") },
                    navigationIcon = {
                        navigationIcon?.invoke()
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            bottomBar?.invoke()
        }
    ) { padding ->
        content(padding)
    }
}
