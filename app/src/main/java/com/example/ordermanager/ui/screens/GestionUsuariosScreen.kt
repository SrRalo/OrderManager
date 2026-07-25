package com.example.ordermanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ordermanager.ui.components.ScreenScaffold
import com.example.ordermanager.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(onBack: () -> Unit) {
    // Mock data for user management
    val users = listOf(
        UserData("Carlos Pérez", "Admin", true),
        UserData("Elena Gómez", "Chef", true),
        UserData("Mario Ruiz", "Mesero", true),
        UserData("Lucía Fernández", "Mesero", false),
        UserData("Roberto Sosa", "Chef", true)
    )

    ScreenScaffold(
        title = "Gestión de Usuarios",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Personal Registrado",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "Administra los accesos y roles del equipo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(users) { user ->
                        UserListItem(user = user)
                    }
                }
            }

            FloatingActionButton(
                onClick = { /* Acción de crear usuario */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Usuario")
            }
        }
    }
}

@Composable
fun UserListItem(user: UserData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = AvatarIconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeColor = when (user.rol) {
                        "Admin" -> Primary
                        "Chef" -> Warning
                        else -> Success
                    }
                    
                    Surface(
                        color = badgeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = user.rol,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 12.sp,
                            color = badgeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    if (!user.activo) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inactivo",
                            fontSize = 12.sp,
                            color = TextDisabled
                        )
                    }
                }
            }

            TextButton(onClick = { /* Editar usuario */ }) {
                Text("Gestionar", color = Primary)
            }
        }
    }
}

data class UserData(
    val nombre: String,
    val rol: String,
    val activo: Boolean
)
