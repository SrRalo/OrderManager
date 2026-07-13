package com.example.ordermanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ordermanager.ui.components.AppTextField
import com.example.ordermanager.ui.components.PrimaryButton
import com.example.ordermanager.ui.theme.*
import com.example.ordermanager.ui.viewmodel.AuthViewModel
import com.example.ordermanager.ui.viewmodel.LoginState

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val loginState by authViewModel.loginState.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState.currentUser) {
        if (authState.currentUser != null) {
            onLoginSuccess()
        }
    }

    val formUsername = if (loginState is LoginState.Form) (loginState as LoginState.Form).username else ""
    val formPassword = if (loginState is LoginState.Form) (loginState as LoginState.Form).password else ""
    val errorMessage = when (loginState) {
        is LoginState.Form -> (loginState as LoginState.Form).errorMessage
        is LoginState.Error -> (loginState as LoginState.Error).message
        else -> null
    }
    val isLoading = loginState is LoginState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.twoXl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.fiveXl))

        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "Accede a tu cuenta",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.fourXl))

        AppTextField(
            value = formUsername,
            onValueChange = { authViewModel.updateLoginUsername(it) },
            label = "Usuario o Correo",
            placeholder = "Usuario o Correo",
            leadingIcon = Icons.Default.Person,
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        AppTextField(
            value = formPassword,
            onValueChange = { authViewModel.updateLoginPassword(it) },
            label = "Contraseña",
            placeholder = "Contraseña",
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            isError = errorMessage != null
        )

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

        Spacer(modifier = Modifier.height(Spacing.threeXl))

        PrimaryButton(
            text = "Ingresar",
            onClick = { authViewModel.login() },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(Spacing.threeXl))

        Text(
            text = buildAnnotatedString {
                append("¿No tienes una cuenta? ")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Regístrate")
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(bottom = Spacing.twoXl)
                .clickable { onNavigateToRegister() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    OrderManagerTheme {
        LoginScreen(
            authViewModel = AuthViewModel(android.app.Application()),
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}
