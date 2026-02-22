package app.compose.appoxxo.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.ui.screens.AddPasswordScreen
import app.compose.appoxxo.ui.screens.AddProductScreen
import app.compose.appoxxo.ui.screens.AlertsScreen
import app.compose.appoxxo.ui.screens.ChangePasswordScreen
import app.compose.appoxxo.ui.screens.DashboardScreen
import app.compose.appoxxo.ui.screens.EditEmailScreen
import app.compose.appoxxo.ui.screens.EditNameScreen
import app.compose.appoxxo.ui.screens.EditProductScreen
import app.compose.appoxxo.ui.screens.HelpScreen
import app.compose.appoxxo.ui.screens.LoginScreen
import app.compose.appoxxo.ui.screens.MovementDetailScreen
import app.compose.appoxxo.ui.screens.MovementsScreen
import app.compose.appoxxo.ui.screens.ProductListScreen
import app.compose.appoxxo.ui.screens.ProfileScreen
import app.compose.appoxxo.ui.screens.RegisterScreen
import app.compose.appoxxo.ui.screens.SecurityPolicyScreen
import app.compose.appoxxo.ui.screens.SplashScreen
import app.compose.appoxxo.ui.screens.UsersScreen
import app.compose.appoxxo.viewmodel.AppViewModelFactory
import app.compose.appoxxo.viewmodel.AuthViewModel
import app.compose.appoxxo.viewmodel.DashboardViewModel
import app.compose.appoxxo.viewmodel.ProductViewModel
import app.compose.appoxxo.viewmodel.UserViewModel
import kotlinx.coroutines.launch

// Rutas que no muestran Scaffold
private val fullScreenRoutes = listOf(
    NavItem.Login.route,
    NavItem.Register.route,
    NavItem.Splash.route  // ← agrega splash
)

private data class DrawerItem(
    val navItem: NavItem,
    val label: String,
    val iconRes: Int,
    val adminOnly: Boolean = false
)

private val drawerItems = listOf(
    DrawerItem(NavItem.Dashboard, "Dashboard",   R.drawable.ic_home),
    DrawerItem(NavItem.Movements, "Movimientos", R.drawable.ic_list),
    DrawerItem(NavItem.Alerts,    "Alertas",     R.drawable.ic_notifications),
    DrawerItem(NavItem.Users,     "Usuarios",    R.drawable.ic_person, adminOnly = true)
)

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onGoogleSignIn: () -> Unit
) {
    val navController = rememberNavController()
    NavGraph(
        navController  = navController,
        authViewModel  = authViewModel,
        onGoogleSignIn = onGoogleSignIn
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onGoogleSignIn: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val factory = AppViewModelFactory(context = context.applicationContext)

    val productViewModel: ProductViewModel     = viewModel(factory = factory)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val userViewModel: UserViewModel           = viewModel(factory = factory)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showScaffold = currentRoute !in fullScreenRoutes

    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == UserRole.ADMIN

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    if (showScaffold) {
        ModalNavigationDrawer(
            drawerState    = drawerState,
            gesturesEnabled = true,
            drawerContent  = {
                AppDrawerContent(
                    currentRoute = currentRoute,
                    isAdmin      = isAdmin,
                    onNavigate   = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                        navController.navigate(NavItem.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        ) {
            Scaffold(
                topBar    = {
                    AppTopBar(
                        currentRoute = currentRoute,
                        onMenuClick  = { scope.launch { drawerState.open() } }
                    )
                },
                bottomBar = { AppBottomBar(navController = navController) }
            ) { innerPadding ->
                AppNavHost(
                    navController      = navController,
                    authViewModel      = authViewModel,
                    productViewModel   = productViewModel,
                    dashboardViewModel = dashboardViewModel,
                    userViewModel      = userViewModel,
                    onGoogleSignIn     = onGoogleSignIn,
                    modifier           = Modifier.padding(innerPadding)
                )
            }
        }
    } else {
        AppNavHost(
            navController      = navController,
            authViewModel      = authViewModel,
            productViewModel   = productViewModel,
            dashboardViewModel = dashboardViewModel,
            userViewModel      = userViewModel,
            onGoogleSignIn     = onGoogleSignIn
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentRoute: String?,
    onMenuClick: () -> Unit
) {
    val title = when {
        currentRoute == NavItem.Dashboard.route                 -> "Dashboard"
        currentRoute == NavItem.ProductList.route               -> "Productos"
        currentRoute == NavItem.AddProduct.route                -> "Agregar Producto"
        currentRoute?.startsWith("edit_product")    == true     -> "Editar Producto"
        currentRoute == NavItem.Movements.route                 -> "Movimientos"
        currentRoute?.startsWith("movement_detail") == true     -> "Movimientos"
        currentRoute == NavItem.Alerts.route                    -> "Alertas"
        currentRoute == NavItem.Profile.route                   -> "Perfil"
        currentRoute == NavItem.Users.route                     -> "Usuarios"
        else                                                    -> "Inventario"
    }
    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Abrir menú"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun AppDrawerContent(
    currentRoute: String?,
    isAdmin: Boolean,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text       = "Inventario",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        drawerItems
            .filter { !it.adminOnly || isAdmin }
            .forEach { item ->
                val selected = currentRoute == item.navItem.route
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painter            = painterResource(id = item.iconRes),
                            contentDescription = item.label
                        )
                    },
                    label = {
                        Text(
                            item.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selected,
                    onClick  = { onNavigate(item.navItem.route) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_exittoapp),
                    contentDescription = "Cerrar sesión",
                    tint               = MaterialTheme.colorScheme.error
                )
            },
            label = {
                Text(
                    "Cerrar sesión",
                    color      = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = false,
            onClick  = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = navBackStackEntry?.destination?.hierarchy
                ?.any { it.route == item.navItem.route } == true
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    navController.navigate(item.navItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon  = {
                    Icon(
                        painter            = painterResource(id = item.iconRes),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    dashboardViewModel: DashboardViewModel,
    userViewModel: UserViewModel,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = NavItem.Splash.route, // ← cambia a Splash
        modifier         = modifier
    ) {
        // ─── Splash ───────────────────────────────────────────────
        composable(NavItem.Splash.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(NavItem.Login.route) {
                        popUpTo(NavItem.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(NavItem.Dashboard.route) {
                        popUpTo(NavItem.Splash.route) { inclusive = true }
                    }
                },
                isLoggedIn = currentUser != null
            )
        }

        // ─── Auth ─────────────────────────────────────────────────
        composable(NavItem.Login.route) {
            LoginScreen(
                viewModel       = authViewModel,
                onLoginSuccess  = {
                    navController.navigate(NavItem.Dashboard.route) {
                        popUpTo(NavItem.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister  = { navController.navigate(NavItem.Register.route) },
                onGoogleSignIn  = onGoogleSignIn
            )
        }
        composable(NavItem.Register.route) {
            RegisterScreen(
                viewModel         = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(NavItem.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGoToLogin    = { navController.popBackStack() },
                onGoogleSignIn = onGoogleSignIn
            )
        }

        // ─── Principal ────────────────────────────────────────────
        composable(NavItem.Dashboard.route) {
            DashboardScreen(
                viewModel            = dashboardViewModel,
                onNavigateToProducts = { navController.navigate(NavItem.ProductList.route) },
                onNavigateToAlerts   = { navController.navigate(NavItem.Alerts.route) }
            )
        }
        composable(NavItem.ProductList.route) {
            ProductListScreen(
                viewModel        = productViewModel,
                authViewModel    = authViewModel,
                onAddProduct     = { navController.navigate(NavItem.AddProduct.route) },
                onEditProduct    = { navController.navigate(NavItem.EditProduct.createRoute(it)) },
                onViewMovements  = { navController.navigate(NavItem.MovementDetail.createRoute(it)) }
            )
        }
        composable(NavItem.AddProduct.route) {
            AddProductScreen(
                viewModel      = productViewModel,
                onProductSaved = { navController.popBackStack() }
            )
        }
        composable(
            route     = NavItem.EditProduct.route,
            arguments = listOf(
                navArgument(NavItem.EditProduct.ARG_PRODUCT_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments
                ?.getString(NavItem.EditProduct.ARG_PRODUCT_ID) ?: return@composable
            EditProductScreen(
                productId        = productId,
                viewModel        = productViewModel,
                onProductUpdated = { navController.popBackStack() }
            )
        }
        composable(NavItem.Movements.route) {
            MovementsScreen(viewModel = productViewModel)
        }
        composable(
            route     = NavItem.MovementDetail.route,
            arguments = listOf(
                navArgument(NavItem.MovementDetail.ARG_PRODUCT_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments
                ?.getString(NavItem.MovementDetail.ARG_PRODUCT_ID) ?: return@composable
            MovementDetailScreen(
                productId = productId,
                viewModel = productViewModel,
                onBack    = { navController.popBackStack() }
            )
        }
        composable(NavItem.Alerts.route) {
            AlertsScreen(viewModel = productViewModel)
        }

        // ─── Perfil ───────────────────────────────────────────────
        composable(NavItem.Profile.route) {
            ProfileScreen(
                viewModel        = authViewModel,
                onLogout         = {
                    authViewModel.logout()
                    navController.navigate(NavItem.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onEditName       = { navController.navigate(NavItem.EditName.route) },
                onEditEmail      = { navController.navigate(NavItem.EditEmail.route) },
                onChangePassword = { navController.navigate(NavItem.ChangePassword.route) },
                onAddPassword    = { navController.navigate(NavItem.AddPassword.route) },
                onSecurityPolicy = { navController.navigate(NavItem.SecurityPolicy.route) }
            )
        }
        composable(NavItem.EditName.route) {
            EditNameScreen(viewModel = authViewModel, onBack = { navController.popBackStack() })
        }
        composable(NavItem.EditEmail.route) {
            EditEmailScreen(viewModel = authViewModel, onBack = { navController.popBackStack() })
        }
        composable(NavItem.ChangePassword.route) {
            ChangePasswordScreen(viewModel = authViewModel, onBack = { navController.popBackStack() })
        }
        composable(NavItem.AddPassword.route) {
            AddPasswordScreen(viewModel = authViewModel, onBack = { navController.popBackStack() })
        }

        // ─── Usuarios ─────────────────────────────────────────────
        composable(NavItem.Users.route) {
            UsersScreen(viewModel = userViewModel)
        }

        // ─── Ayuda y Políticas ────────────────────────────────────
        composable(NavItem.Help.route) {
            HelpScreen(onBack = { navController.popBackStack() })
        }
        composable(NavItem.SecurityPolicy.route) {
            SecurityPolicyScreen(onBack = { navController.popBackStack() })
        }
    }
}