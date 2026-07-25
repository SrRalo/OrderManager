package com.example.ordermanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ordermanager.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class BalanceSummary(
    val label: String,
    val amount: String,
    val sublabel: String,
    val accentColor: androidx.compose.ui.graphics.Color
)

private data class RecentTransaction(
    val id: String,
    val cliente: String,
    val monto: Double,
    val hora: String
)

private val mockSummaries = listOf(
    BalanceSummary("Hoy", "$345.50", "+12% que ayer", Success),
    BalanceSummary("Esta semana", "$2,150.00", "+8% que la semana pasada", Primary),
    BalanceSummary("Este mes", "$8,432.00", "+5% que el mes pasado", Warning)
)

private val mockTransactions = listOf(
    RecentTransaction("ORD-0010", "María García", 18.50, "12:30"),
    RecentTransaction("ORD-0011", "Luis Torres", 24.48, "12:15"),
    RecentTransaction("ORD-0012", "Valentina Ortiz", 9.99, "11:50"),
    RecentTransaction("ORD-0013", "Diego Castillo", 32.00, "11:20"),
    RecentTransaction("ORD-0014", "Sofía Ramírez", 15.75, "10:45"),
    RecentTransaction("ORD-0015", "José Hernández", 22.30, "10:10")
)

@Composable
fun BalancesScreen() {
    val today = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("es-MX")).format(Date())
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg)
        ) {
            item {
                Text(
                    text = "Balances",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = today.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Spacing.twoXl))
            }

            item {
                TodayEarningsCard()

                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    mockSummaries.forEach { summary ->
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            summary = summary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.twoXl))
            }

            item {
                Text(
                    text = "Últimas transacciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Spacing.md))
            }

            items(mockTransactions) { tx ->
                TransactionItem(transaction = tx)
                Spacer(modifier = Modifier.height(Spacing.sm))
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.lg))
            }
        }
    }
}

@Composable
private fun TodayEarningsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = Shapes.cardLarge,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
        shape = Shapes.cardLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.twoXl)
        ) {
            Text(
                text = "Ingresos del día",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "$345.50",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+12%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "vs. ayer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Pedidos", value = "15", onPrimary = true)
                StatItem(label = "Ticket Prom.", value = "$23.03", onPrimary = true)
                StatItem(label = "Pendientes", value = "2", onPrimary = true)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    summary: BalanceSummary
) {
    Card(
        modifier = modifier,
        shape = Shapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = summary.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = summary.amount,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = summary.sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = summary.accentColor
            )
        }
    }
}

@Composable
private fun TransactionItem(transaction: RecentTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.card)
            .background(MaterialTheme.colorScheme.surface)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(Radius.md))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\uD83D\uDCE6",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.cliente,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = transaction.id,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${String.format("%.2f", transaction.monto)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = transaction.hora,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    onPrimary: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (onPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (onPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
