package app.compose.appoxxo.ui.navigation



import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.compose.appoxxo.data.ServiceLocator
import app.compose.appoxxo.ui.screens.AddProductScreen
import app.compose.appoxxo.ui.screens.DashboardScreen
import app.compose.appoxxo.ui.screens.EditProductScreen
import app.compose.appoxxo.ui.screens.LoginScreen
import app.compose.appoxxo.ui.screens.MovementScreen
import app.compose.appoxxo.ui.screens.ProductListScreen
import app.compose.appoxxo.ui.screens.RegisterScreen
import app.compose.appoxxo.viewmodel.AppViewModelFactory
import app.compose.appoxxo.viewmodel.AuthViewModel
import app.compose.appoxxo.viewmodel.DashboardViewModel
import app.compose.appoxxo.viewmodel.ProductViewModel


@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavGraph(navController = navController)
}

@Composable
fun NavGraph(navController: NavHostController) {

    val factory = AppViewModelFactory(
        authRepository = ServiceLocator.authRepository,
        productRepository = ServiceLocator.productRepository
    )

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val productViewModel: ProductViewModel = viewModel(factory = factory)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = NavItem.Login.route
    ) {

        //Auth
        composable(NavItem.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(NavItem.Dashboard.route) {
                        popUpTo(NavItem.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = {
                    navController.navigate(NavItem.Register.route)
                }
            )
        }

        composable(NavItem.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(NavItem.Dashboard.route) {
                        popUpTo(NavItem.Login.route) { inclusive = true }
                    }
                },
                onGoToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Dashboard

        composable(NavItem.Dashboard.route) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToProducts = {
                    navController.navigate(NavItem.ProductList.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavItem.Login.route) {
                        popUpTo(NavItem.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        //Productos
        composable(NavItem.ProductList.route) {
            ProductListScreen(
                viewModel = productViewModel,
                onAddProduct = {
                    navController.navigate(NavItem.AddProduct.route)
                },
                onEditProduct = { productId ->
                    navController.navigate(NavItem.EditProduct.createRoute(productId))
                },
                onViewMovements = { productId ->
                    navController.navigate(NavItem.Movements.createRoute(productId))
                }
            )
        }

        composable(NavItem.AddProduct.route) {
            AddProductScreen(
                viewModel = productViewModel,
                onProductSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavItem.EditProduct.route,
            arguments = listOf(
                navArgument(NavItem.EditProduct.ARG_PRODUCT_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments
                ?.getString(NavItem.EditProduct.ARG_PRODUCT_ID)
                ?: return@composable
            EditProductScreen(
                productId = productId,
                viewModel = productViewModel,
                onProductUpdated = {
                    navController.popBackStack()
                }
            )
        }

        // Movimientos
        composable(
            route = NavItem.Movements.route,
            arguments = listOf(
                navArgument(NavItem.Movements.ARG_PRODUCT_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments
                ?.getString(NavItem.Movements.ARG_PRODUCT_ID)
                ?: return@composable
            MovementScreen(
                productId = productId,
                viewModel = productViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
