package com.example.ordermanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ordermanager.backend.data.model.UsuarioRow
import com.example.ordermanager.ui.components.ScreenScaffold
import com.example.ordermanager.ui.theme.*
import com.example.ordermanager.ui.viewmodel.UserManagementViewModel

private val roles = listOf("admin", "chef", "mesero", "supervisor")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    onBack: () -> Unit,
    userManagementViewModel: UserManagementViewModel
) {
    val uiState = userManagementViewModel.uiState.collectAsStateWithLifecycle().value
    var userToManage by remember { mutableStateOf<UsuarioRow?>(null) }

    LaunchedEffect(uiState.operacionExitosa) {
        if (uiState.operacionExitosa) {
            userToManage = null
            userManagementViewModel.limpiarEstado()
        }
    }

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
            Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                Text(
                    text = "Usuarios",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.lg)
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )
                }

                if (uiState.isLoading && uiState.usuarios.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else {
                    val uniqueUsers = remember(uiState.usuarios) { uiState.usuarios.distinctBy { it.id } }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uniqueUsers, key = { it.id }) { user ->
                            UserListItem(
                                user = user,
                                onManage = { userToManage = user }
                            )
                        }
                    }
                }
            }
        }
    }

    userToManage?.let { user ->
        UserManageDialog(
            user = user,
            onDismiss = { userToManage = null },
            onChangeRole = { nuevoRol ->
                userManagementViewModel.cambiarRol(user.id, nuevoRol)
            },
            onToggleActive = { activo ->
                userManagementViewModel.setUsuarioActivo(user.id, activo)
            }
        )
    }
}

@Composable
fun UserListItem(
    user: UsuarioRow,
    onManage: () -> Unit
) {
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
                    text = user.nombres,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeColor = when (user.rol) {
                        "admin" -> Primary
                        "chef" -> Warning
                        "supervisor" -> Color(0xFF8B5CF6)
                        else -> Success
                    }

                    Surface(
                        color = badgeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = user.rol.replaceFirstChar { it.uppercase() },
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

                Text(
                    text = user.usuario,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            TextButton(onClick = onManage) {
                Text("Gestionar", color = Primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManageDialog(
    user: UsuarioRow,
    onDismiss: () -> Unit,
    onChangeRole: (String) -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    var expandedRole by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Gestionar usuario",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    text = user.nombres,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.usuario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = it }
                ) {
                    OutlinedTextField(
                        value = user.rol.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        roles.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onChangeRole(rol)
                                    expandedRole = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activo", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = user.activo,
                        onCheckedChange = { onToggleActive(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Primary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
