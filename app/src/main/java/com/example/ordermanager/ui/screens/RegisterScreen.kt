package com.example.ordermanager.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.ordermanager.ui.components.AppTextField
import com.example.ordermanager.ui.components.PrimaryButton
import com.example.ordermanager.ui.components.ScreenScaffold
import com.example.ordermanager.ui.theme.*
import com.example.ordermanager.ui.viewmodel.AuthViewModel
import com.example.ordermanager.ui.viewmodel.RegisterState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val registerState by authViewModel.registerState.collectAsStateWithLifecycle()
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

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Success) {
            Toast.makeText(context, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
            onRegisterSuccess()
        }
    }

    val form = if (registerState is RegisterState.Form) registerState as RegisterState.Form else null
    val errorMessage = when (registerState) {
        is RegisterState.Form -> form?.errorMessage
        is RegisterState.Error -> (registerState as RegisterState.Error).message
        else -> null
    }
    val isLoading = registerState is RegisterState.Loading

    ScreenScaffold(
        title = "Registrar Nuevo Usuario",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
            }
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

            AppTextField(
                value = form?.nombres ?: "",
                onValueChange = { authViewModel.updateRegisterNombres(it) },
                label = "Nombres",
                placeholder = "Nombres completos"
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            AppTextField(
                value = form?.correo ?: "",
                onValueChange = { authViewModel.updateRegisterCorreo(it) },
                label = "Correo Electrónico",
                placeholder = "correo@ejemplo.com"
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            AppTextField(
                value = form?.usuario ?: "",
                onValueChange = { authViewModel.updateRegisterUsuario(it) },
                label = "Usuario",
                placeholder = "Nombre de usuario"
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            AppTextField(
                value = form?.contrasena ?: "",
                onValueChange = { authViewModel.updateRegisterContrasena(it) },
                label = "Contraseña",
                placeholder = "Contraseña",
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            AppTextField(
                value = form?.confirmarContrasena ?: "",
                onValueChange = { authViewModel.updateRegisterConfirmarContrasena(it) },
                label = "Confirmar Contraseña",
                placeholder = "Repite la contraseña",
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            AppTextField(
                value = form?.telefono ?: "",
                onValueChange = { authViewModel.updateRegisterTelefono(it) },
                label = "Teléfono",
                placeholder = "Número de teléfono"
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.card,
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
                        tint = if (ubicacionCapturada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Column {
                        Text(
                            text = if (ubicacionCapturada) "Ubicación capturada"
                                   else "Ubicación no capturada",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (form?.latitud != null && form.longitud != null) {
                            Text(
                                text = "Lat: %.4f, Lng: %.4f".format(form.latitud, form.longitud),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.twoXl))

            PrimaryButton(
                text = "Guardar",
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
                        return@PrimaryButton
                    }
                    authViewModel.registrar()
                },
                isLoading = isLoading
            )

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
