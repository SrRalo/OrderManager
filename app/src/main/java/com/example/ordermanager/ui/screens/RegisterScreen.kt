package com.example.ordermanager.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ordermanager.ui.theme.*
import com.example.ordermanager.ui.viewmodel.AuthViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(authViewModel: AuthViewModel) {
    val uiState by authViewModel.registerUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var ubicacionCapturada by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            capturarUbicacion(fusedLocationClient, authViewModel) { ubicacionCapturada = it }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineGranted == PackageManager.PERMISSION_GRANTED || coarseGranted == PackageManager.PERMISSION_GRANTED) {
            capturarUbicacion(fusedLocationClient, authViewModel) { ubicacionCapturada = it }
        }
    }

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            Toast.makeText(context, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
            authViewModel.navigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Nuevo Usuario") },
                navigationIcon = {
                    IconButton(onClick = { authViewModel.navigateToLogin() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = Spacing.twoXl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = uiState.nombres,
                onValueChange = { authViewModel.updateRegisterNombres(it) },
                label = { Text("Nombres") },
                placeholder = { Text("Nombres completos") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = uiState.correo,
                onValueChange = { authViewModel.updateRegisterCorreo(it) },
                label = { Text("Correo Electrónico") },
                placeholder = { Text("correo@ejemplo.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = uiState.usuario,
                onValueChange = { authViewModel.updateRegisterUsuario(it) },
                label = { Text("Usuario") },
                placeholder = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = uiState.contrasena,
                onValueChange = { authViewModel.updateRegisterContrasena(it) },
                label = { Text("Contraseña") },
                placeholder = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = uiState.confirmarContrasena,
                onValueChange = { authViewModel.updateRegisterConfirmarContrasena(it) },
                label = { Text("Confirmar Contraseña") },
                placeholder = { Text("Repite la contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = uiState.telefono,
                onValueChange = { authViewModel.updateRegisterTelefono(it) },
                label = { Text("Teléfono") },
                placeholder = { Text("Número de teléfono") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (ubicacionCapturada) MaterialTheme.colorScheme.primary else TextDisabled
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Column {
                        Text(
                            text = if (ubicacionCapturada) "Ubicación capturada"
                                   else "Ubicación no capturada",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (uiState.latitud != null && uiState.longitud != null) {
                            Text(
                                text = "Lat: %.4f, Lng: %.4f".format(uiState.latitud, uiState.longitud),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.twoXl))

            Button(
                onClick = {
                    if (!ubicacionCapturada) {
                        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        if (fineGranted == PackageManager.PERMISSION_GRANTED || coarseGranted == PackageManager.PERMISSION_GRANTED) {
                            capturarUbicacion(fusedLocationClient, authViewModel) { ubicacionCapturada = it }
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                        return@Button
                    }
                    authViewModel.registrar()
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(Radius.lg),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Guardar",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.twoXl))
        }
    }
}

private fun capturarUbicacion(
    fusedLocationClient: FusedLocationProviderClient,
    authViewModel: AuthViewModel,
    onCapturada: (Boolean) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            authViewModel.updateLocation(location.latitude, location.longitude)
            onCapturada(true)
        } else {
            onCapturada(false)
        }
    }.addOnFailureListener {
        onCapturada(false)
    }
}
