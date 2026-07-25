package com.example.ordermanager

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ordermanager.data.local.AppDatabase
import com.example.ordermanager.data.local.entity.MenuItemEntity
import com.example.ordermanager.data.local.entity.UsuarioEntity
import com.example.ordermanager.data.repository.UsuarioRepository
import com.example.ordermanager.ui.navigation.NavRoutes
import com.example.ordermanager.ui.screens.*
import com.example.ordermanager.ui.theme.OrderManagerTheme
import com.example.ordermanager.ui.viewmodel.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private val menuViewModel: MenuViewModel by viewModels()
    private val userManagementViewModel: UserManagementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        initializeTestUsers()
        initializeMenu()

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            val authState by authViewModel.authState.collectAsState()

            OrderManagerTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.SPLASH,
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() }
                ) {
                    composable(NavRoutes.SPLASH) {
                        SplashScreen(onSplashFinished = {
                            navController.navigate(NavRoutes.WELCOME) {
                                popUpTo(NavRoutes.SPLASH) { inclusive = true }
                            }
                        })
                    }

                    composable(NavRoutes.WELCOME) {
                        WelcomeScreen(
                            onNavigateToLogin = { navController.navigate(NavRoutes.LOGIN) },
                            onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) }
                        )
                    }

                    composable(NavRoutes.LOGIN) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(NavRoutes.MAIN_HUB) {
                                    popUpTo(NavRoutes.WELCOME) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                            authViewModel = authViewModel
                        )
                    }

                    composable(NavRoutes.REGISTER) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(NavRoutes.REGISTER) { inclusive = true }
                                }
                            },
                            onBack = { navController.navigateUp() },
                            authViewModel = authViewModel
                        )
                    }

                    composable(NavRoutes.MAIN_HUB) {
                        val currentUser = authState.currentUser
                        if (currentUser != null) {
                            MainHubScreen(
                                orderViewModel = orderViewModel,
                                menuViewModel = menuViewModel,
                                userManagementViewModel = userManagementViewModel,
                                currentUserId = currentUser.supabaseId,
                                usuario = currentUser.usuario,
                                userRole = UserRole.valueOf(currentUser.rol.uppercase()),
                                isDarkMode = isDarkMode,
                                onToggleTheme = { isDarkMode = !isDarkMode },
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate(NavRoutes.WELCOME) {
                                        popUpTo(NavRoutes.MAIN_HUB) { inclusive = true }
                                    }
                                },
                                onCreateUserByAdmin = { nombres, correo, usuario, telefono, contrasena, rol, onResult ->
                                    authViewModel.createUserByAdmin(nombres, correo, usuario, telefono, contrasena, rol, onResult)
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(NavRoutes.MAIN_HUB) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initializeTestUsers() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getInstance(this@MainActivity)
                val repository = UsuarioRepository(database.usuarioDao())
                
                val existingCount = repository.contarUsuarios()
                if (existingCount > 0) {
                    Log.d("MainActivity", "Test users not initialized - $existingCount users already exist locally")
                    return@launch
                }

                Log.w("MainActivity", "🔧 INITIALIZING DEVELOPMENT TEST USERS")

                val testPassword = "SecureTestPass123!"

                data class TestUser(val email: String, val username: String, val role: UserRole)
                
                val testUsers = listOf(
                    TestUser("admin@test.com", "admin", UserRole.ADMIN),
                    TestUser("supervisor@test.com", "supervisor", UserRole.SUPERVISOR),
                    TestUser("mesero@test.com", "mesero", UserRole.MESERO),
                    TestUser("chef@test.com", "chef", UserRole.CHEF)
                )

                for (testUser in testUsers) {
                    val usuario = UsuarioEntity(
                        nombres = "Test ${testUser.username.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}",
                        correo = testUser.email,
                        usuario = testUser.username,
                        contrasena = testPassword,
                        telefono = "+1234567890",
                        fechaRegistro = System.currentTimeMillis(),
                        latitud = 0.0,
                        longitud = 0.0,
                        rol = testUser.role.name.lowercase()
                    )

                    repository.registrar(usuario)
                }
                Log.i("MainActivity", "🌱 Test user initialization completed")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error during test user initialization: ${e.message}")
            }
        }
    }

    private fun initializeMenu() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getInstance(this@MainActivity)
                val dao = database.menuItemDao()
                
                val count = dao.getAllItems().first().size
                if (count > 0) {
                    Log.d("MainActivity", "Menu already initialized with $count items")
                    return@launch
                }

                Log.w("MainActivity", "🍱 INITIALIZING DEFAULT MENU")

                val items = listOf(
                    MenuItemEntity(1, "Hamburguesa Clásica", "Carne de res, queso y vegetales", 8.50, 1, "Hamburguesa Clásica"),
                    MenuItemEntity(2, "Papas Fritas Grandes", "Papas fritas crujientes con sal", 4.00, 6, "Papas Fritas Grandes"),
                    MenuItemEntity(3, "Pizza Pepperoni", "Mozzarella y abundante pepperoni", 12.00, 2, "Pizza Pepperoni"),
                    MenuItemEntity(4, "Tacos al Pastor", "3 tacos con piña, cebolla y cilantro", 7.50, 1, "Tacos al Pastor"),
                    MenuItemEntity(5, "Ensalada César", "Lechuga, croutones y aderezo césar", 7.00, 3, "Ensalada César"),
                    MenuItemEntity(6, "Refresco de Cola", "Lata de 355ml bien fría", 2.00, 4, "Refresco de Cola"),
                    MenuItemEntity(7, "Agua Natural", "Botella de 500ml", 1.50, 4, "Agua Natural"),
                    MenuItemEntity(8, "Flan Napolitano", "Postre casero con caramelo", 4.50, 5, "Flan Napolitano"),
                    MenuItemEntity(9, "Quesadillas", "Tortilla de harina con queso fundido", 6.00, 1, "Quesadillas"),
                    MenuItemEntity(10, "Burrito Supreme", "Relleno de carne, frijoles y arroz", 9.00, 1, "Burrito Supreme")
                )

                for (item in items) {
                    dao.insertar(item)
                }
                Log.i("MainActivity", "🌱 Menu seeding completed")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error during menu initialization: ${e.message}")
            }
        }
    }
}
