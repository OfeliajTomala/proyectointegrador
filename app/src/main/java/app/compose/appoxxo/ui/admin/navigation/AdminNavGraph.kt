package app.compose.appoxxo.ui.admin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.ui.NavItem
import app.compose.appoxxo.ui.admin.screens.UsersScreen
import app.compose.appoxxo.ui.components.AppDrawerContent
import app.compose.appoxxo.ui.screens.*
import app.compose.appoxxo.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val factory = AppViewModelFactory(context.applicationContext)

    val productViewModel: ProductViewModel     = viewModel(factory = factory)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val userViewModel: UserViewModel           = viewModel(factory = factory)

    val currentUser        by authViewModel.currentUser.collectAsState()
    val navBackStackEntry  by navController.currentBackStackEntryAsState()
    val currentRoute        = navBackStackEntry?.destination?.route
    val drawerState         = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope               = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState     = drawerState,
        gesturesEnabled = true,
        drawerContent   = {
            AppDrawerContent(
                currentRoute = currentRoute,
                isAdmin      = currentUser?.role == UserRole.ADMIN,
                currentUser  = currentUser,
                onNavigate   = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when {
                                currentRoute == NavItem.Dashboard.route              -> "Dashboard"
                                currentRoute == NavItem.ProductList.route            -> "Productos"
                                currentRoute == NavItem.AddProduct.route             -> "Agregar Producto"
                                currentRoute?.startsWith("edit_product") == true     -> "Editar Producto"
                                currentRoute == NavItem.Movements.route              -> "Movimientos"
                                currentRoute?.startsWith("movement_detail") == true  -> "Movimientos"
                                currentRoute == NavItem.Alerts.route                 -> "Alertas"
                                currentRoute == NavItem.Users.route                  -> "Usuarios"
                                currentRoute == NavItem.Profile.route                -> "Perfil"
                                currentRoute == NavItem.EditName.route               -> "Cambiar Nombre"
                                currentRoute == NavItem.EditEmail.route              -> "Cambiar Correo"
                                currentRoute == NavItem.ChangePassword.route         -> "Cambiar Contraseña"
                                currentRoute == NavItem.AddPassword.route            -> "Agregar Contraseña"
                                currentRoute == NavItem.SecurityPolicy.route         -> "Políticas de Seguridad"
                                currentRoute == NavItem.Help.route                   -> "Ayuda"
                                else                                                 -> "Inventario"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
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
            },
            bottomBar = { AppBottomBarAdmin(navController, dashboardViewModel) }
        ) { innerPadding ->

            NavHost(
                navController    = navController,
                startDestination = NavItem.Dashboard.route,
                modifier         = Modifier.padding(innerPadding)
            ) {

                composable(NavItem.Dashboard.route) {
                    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
                    LaunchedEffect(lifecycle) {
                        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
                            dashboardViewModel.loadStats()
                        }
                    }
                    DashboardScreen(
                        viewModel            = dashboardViewModel,
                        onNavigateToProducts = { navController.navigate(NavItem.ProductList.route) },
                        onNavigateToAlerts   = { navController.navigate(NavItem.Alerts.route) }
                    )
                }

                composable(NavItem.ProductList.route) {
                    ProductListScreen(
                        viewModel       = productViewModel,
                        authViewModel   = authViewModel,
                        onAddProduct    = { navController.navigate(NavItem.AddProduct.route) },
                        onEditProduct   = { navController.navigate(NavItem.EditProduct.createRoute(it)) },
                        onViewMovements = { navController.navigate(NavItem.MovementDetail.createRoute(it)) }
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
                        navArgument(NavItem.EditProduct.ARG_PRODUCT_ID) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    EditProductScreen(
                        productId        = backStackEntry.arguments
                            ?.getString(NavItem.EditProduct.ARG_PRODUCT_ID) ?: return@composable,
                        viewModel        = productViewModel,
                        onProductUpdated = { navController.popBackStack() }
                    )
                }

                composable(NavItem.Movements.route) {
                    MovementsScreen(viewModel = productViewModel, canDelete = true)
                }

                composable(
                    route     = NavItem.MovementDetail.route,
                    arguments = listOf(
                        navArgument(NavItem.MovementDetail.ARG_PRODUCT_ID) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    MovementDetailScreen(
                        productId = backStackEntry.arguments
                            ?.getString(NavItem.MovementDetail.ARG_PRODUCT_ID) ?: return@composable,
                        viewModel = productViewModel,
                        onBack    = { navController.popBackStack() },
                        canDelete = true
                    )
                }

                composable(NavItem.Alerts.route) {
                    AlertsScreen(viewModel = productViewModel)
                }

                composable(NavItem.Users.route) {
                    UsersScreen(viewModel = userViewModel)
                }

                composable(NavItem.Profile.route) {
                    LaunchedEffect(Unit) {
                        authViewModel.syncEmail()
                        authViewModel.syncUser()
                    }
                    ProfileScreen(
                        viewModel        = authViewModel,
                        onLogout         = onLogout,
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
                composable(NavItem.SecurityPolicy.route) {
                    SecurityPolicyScreen(onBack = { navController.popBackStack() })
                }
                composable(NavItem.Help.route) {
                    HelpScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun AppBottomBarAdmin(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel
) {
    val entry   by navController.currentBackStackEntryAsState()
    val current  = entry?.destination?.route

    NavigationBar {
        listOf(
            Triple(NavItem.Dashboard.route,   "Home",      R.drawable.ic_home),
            Triple(NavItem.ProductList.route, "Productos", R.drawable.ic_shopping_cart),
            Triple(NavItem.Alerts.route,      "Alertas",   R.drawable.ic_notifications),
            Triple(NavItem.Help.route,        "Ayuda",     R.drawable.ic_help),
            Triple(NavItem.Profile.route,     "Perfil",    R.drawable.ic_person)
        ).forEach { (route, label, icon) ->
            val isDashboard = route == NavItem.Dashboard.route
            NavigationBarItem(
                selected = current == route,
                onClick  = {
                    navController.navigate(route) {
                        // FIX: popUpTo con inclusive = false limpia pantallas apiladas
                        // (como MovementDetail) y vuelve a la raíz de esa pestaña
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = !isDashboard
                        }
                        launchSingleTop = true
                        restoreState    = !isDashboard
                    }
                    if (isDashboard) dashboardViewModel.loadStats()
                },
                icon  = { Icon(painterResource(icon), label) },
                label = { Text(label) }
            )
        }
    }
}