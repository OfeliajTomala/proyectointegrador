package app.compose.appoxxo.ui.cajero.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import app.compose.appoxxo.R
import app.compose.appoxxo.ui.NavItem
import app.compose.appoxxo.ui.cajero.screens.CajeroDrawerContent
import app.compose.appoxxo.ui.cajero.screens.ResumenScreen
import app.compose.appoxxo.ui.screens.*
import app.compose.appoxxo.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CajeroNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val factory = AppViewModelFactory(context.applicationContext)

    val productViewModel: ProductViewModel     = viewModel(factory = factory)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)

    val currentUser       by authViewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute       = navBackStackEntry?.destination?.route
    val drawerState        = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope              = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState     = drawerState,
        gesturesEnabled = true,
        drawerContent   = {
            CajeroDrawerContent(
                currentRoute = currentRoute,
                currentUser = currentUser,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
                                currentRoute == NavItem.ProductList.route           -> "Productos"
                                currentRoute == NavItem.Resumen.route               -> "Resumen"
                                currentRoute == NavItem.Movements.route             -> "Movimientos"
                                currentRoute?.startsWith("movement_detail") == true -> "Movimientos"
                                currentRoute == NavItem.Alerts.route                -> "Alertas"
                                currentRoute == NavItem.Profile.route               -> "Perfil"
                                currentRoute == NavItem.EditName.route              -> "Cambiar Nombre"
                                currentRoute == NavItem.EditEmail.route             -> "Cambiar Correo"
                                currentRoute == NavItem.ChangePassword.route        -> "Cambiar Contraseña"
                                currentRoute == NavItem.AddPassword.route           -> "Agregar Contraseña"
                                currentRoute == NavItem.SecurityPolicy.route        -> "Políticas de Seguridad"
                                currentRoute == NavItem.Help.route                  -> "Ayuda"
                                else                                                -> "Inventario"
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
            bottomBar = { AppBottomBarCajero(navController) }
        ) { innerPadding ->

            NavHost(
                navController    = navController,
                startDestination = NavItem.ProductList.route,
                modifier         = Modifier.padding(innerPadding)
            ) {

                // ── Productos (inicio) ────────────────────────────
                composable(NavItem.ProductList.route) {
                    ProductListScreen(
                        viewModel       = productViewModel,
                        authViewModel   = authViewModel,
                        onAddProduct    = { },
                        onEditProduct   = { },
                        onViewMovements = { navController.navigate(NavItem.MovementDetail.createRoute(it)) }
                    )
                }

                // ── Resumen ───────────────────────────────────────
                composable(NavItem.Resumen.route) {
                    LaunchedEffect(Unit) { dashboardViewModel.loadStats() }
                    ResumenScreen(
                        viewModel            = dashboardViewModel,
                        onNavigateToProducts = { navController.navigate(NavItem.ProductList.route) },
                        onNavigateToAlerts   = { navController.navigate(NavItem.Alerts.route) }
                    )
                }

                // ── Movimientos ───────────────────────────────────
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

                // ── Alertas ───────────────────────────────────────
                composable(NavItem.Alerts.route) {
                    AlertsScreen(viewModel = productViewModel)
                }

                // ── Perfil y sub-pantallas ────────────────────────
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
                    HelpScreen()
                }
            }
        }
    }
}

@Composable
private fun AppBottomBarCajero(navController: NavHostController) {
    val entry   by navController.currentBackStackEntryAsState()
    val current  = entry?.destination?.route

    NavigationBar {
        listOf(
            Triple(NavItem.ProductList.route, "Productos",   R.drawable.ic_shopping_cart),
            Triple(NavItem.Movements.route,   "Movimientos", R.drawable.ic_list),
            Triple(NavItem.Alerts.route,      "Alertas",     R.drawable.ic_notifications),
            Triple(NavItem.Profile.route,     "Perfil",      R.drawable.ic_person)
        ).forEach { (route, label, icon) ->
            val isStart = route == NavItem.ProductList.route
            NavigationBarItem(
                selected = current == route,
                onClick  = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = !isStart
                        }
                        launchSingleTop = true
                        restoreState    = !isStart
                    }
                },
                icon  = { Icon(painterResource(icon), label) },
                label = { Text(label) }
            )
        }
    }
}