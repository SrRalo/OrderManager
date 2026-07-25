package com.example.ordermanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ordermanager.ui.components.AppTextField
import com.example.ordermanager.ui.components.ConfirmModal
import com.example.ordermanager.ui.components.PrimaryButton
import com.example.ordermanager.ui.theme.*
import com.example.ordermanager.ui.viewmodel.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    negocioNombre: String,
    telegramBotId: String,
    usuario: String,
    userRole: UserRole,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onCreateUserByAdmin: (nombres: String, correo: String, usuario: String, telefono: String, contrasenaTemporal: String, rol: UserRole, onResult: (Result<Long>) -> Unit) -> Unit,
    onNegocioNombreChange: (String) -> Unit = {}
) {
    var showSettingsMenu by remember { mutableStateOf(false) }
    val isAdmin = userRole == UserRole.ADMIN

    var editableNegocioNombre by remember { mutableStateOf(negocioNombre) }
    var isEditingNombre by remember { mutableStateOf(false) }

    var showLogoutConfirm by remember { mutableStateOf(false) }

    var expandedSection by remember { mutableStateOf<String?>(null) }

    var nombres by remember { mutableStateOf("") }
    var contrasenaTemporal by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CHEF) }
    var roleExpanded by remember { mutableStateOf(false) }
    var createUserMessage by remember { mutableStateOf<String?>(null) }
    var isCreateUserError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
    ) {
        Spacer(modifier = Modifier.height(Spacing.twoXl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                IconButton(onClick = { showSettingsMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                DropdownMenu(
                    expanded = showSettingsMenu,
                    onDismissRequest = { showSettingsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Text(text = if (isDarkMode) "Modo Claro" else "Modo Oscuro")
                            }
                        },
                        onClick = {
                            onToggleTheme()
                            showSettingsMenu = false
                        }
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = Shapes.avatar
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.lg))
                Column(modifier = Modifier.weight(1f)) {
                    if (isEditingNombre) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = editableNegocioNombre,
                                onValueChange = { editableNegocioNombre = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = {
                                onNegocioNombreChange(editableNegocioNombre)
                                isEditingNombre = false
                            }) {
                                Icon(Icons.Rounded.Check, contentDescription = "Guardar", tint = Success)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = editableNegocioNombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { isEditingNombre = true }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Editar nombre", tint = Primary)
                            }
                        }
                    }
                    Text(
                        text = "Ordenando la cocina",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.threeXl))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedSection = if (expandedSection == "info") null else "info"
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Información del Negocio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expandedSection == "info") Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
                AnimatedVisibility(visible = expandedSection == "info") {
                    Column {
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        InfoRow(label = "Nombre", value = editableNegocioNombre)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        InfoRow(label = "ID Bot Telegram", value = telegramBotId)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        InfoRow(label = "Usuario", value = usuario)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        InfoRow(label = "Rol", value = userRole.toDisplayName())
                    }
                }
            }
        }

        if (isAdmin) {
            Spacer(modifier = Modifier.height(Spacing.twoXl))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedSection = if (expandedSection == "crear") null else "crear"
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = Primary)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Crear usuario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expandedSection == "crear") Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                    AnimatedVisibility(visible = expandedSection == "crear") {
                        Column {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "Asigna rol: Chef, Mesero o Supervisor",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(Spacing.lg))

                            AppTextField(
                                value = nombres,
                                onValueChange = { nombres = it; createUserMessage = null },
                                label = "Nombre Completo",
                                placeholder = "Ej. Ana Gómez",
                                leadingIcon = Icons.Rounded.Person
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            
                            AppTextField(
                                value = contrasenaTemporal,
                                onValueChange = { contrasenaTemporal = it; createUserMessage = null },
                                label = "Contraseña",
                                placeholder = "********",
                                leadingIcon = Icons.Rounded.Lock
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))

                            ExposedDropdownMenuBox(
                                expanded = roleExpanded,
                                onExpandedChange = { roleExpanded = !roleExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedRole.toDisplayName(),
                                    onValueChange = {},
                                    modifier = Modifier
                                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                                        .fillMaxWidth(),
                                    label = { Text("Rol") },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                                    shape = Shapes.input,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = roleExpanded,
                                    onDismissRequest = { roleExpanded = false }
                                ) {
                                    listOf(UserRole.CHEF, UserRole.MESERO, UserRole.SUPERVISOR).forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role.toDisplayName()) },
                                            onClick = {
                                                selectedRole = role
                                                roleExpanded = false
                                                createUserMessage = null
                                            }
                                        )
                                    }
                                }
                            }

                            if (createUserMessage != null) {
                                Spacer(modifier = Modifier.height(Spacing.sm))
                                Text(
                                    text = createUserMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCreateUserError) MaterialTheme.colorScheme.error else Success
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.lg))

                            PrimaryButton(
                                text = "Crear usuario",
                                onClick = {
                                    if (nombres.isBlank() || contrasenaTemporal.isBlank()) {
                                        createUserMessage = "Nombre y contraseña son obligatorios"
                                        isCreateUserError = true
                                        return@PrimaryButton
                                    }
                                    
                                    // Use name as username for simplicity since we simplified the form
                                    val generatedUsername = nombres.lowercase().replace(" ", "")
                                    val dummyEmail = "$generatedUsername@restaurante.com"
                                    
                                    onCreateUserByAdmin(
                                        nombres,
                                        dummyEmail,
                                        generatedUsername,
                                        "N/A",
                                        contrasenaTemporal,
                                        selectedRole
                                    ) { result ->
                                        result.fold(
                                            onSuccess = {
                                                createUserMessage = "Usuario creado: $generatedUsername"
                                                isCreateUserError = false
                                                nombres = ""
                                                contrasenaTemporal = ""
                                                selectedRole = UserRole.CHEF
                                            },
                                            onFailure = { error ->
                                                createUserMessage = error.message ?: "No se pudo crear el usuario"
                                                isCreateUserError = true
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.threeXl))

        OutlinedButton(
            onClick = { showLogoutConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = Shapes.button,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Primary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = "Cerrar Sesión",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(Spacing.fiveXl))
    }

    if (showLogoutConfirm) {
        ConfirmModal(
            visible = true,
            titulo = "¿Cerrar Sesión?",
            onConfirm = {
                showLogoutConfirm = false
                onLogout()
            },
            onCancel = { showLogoutConfirm = false }
        )
    }
}

private fun UserRole.toDisplayName(): String {
    return when (this) {
        UserRole.ADMIN -> "Admin"
        UserRole.CHEF -> "Chef"
        UserRole.MESERO -> "Mesero"
        UserRole.SUPERVISOR -> "Supervisor"
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
