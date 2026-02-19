package app.compose.appoxxo.ui.navigation


import app.compose.appoxxo.R

sealed class NavItem(val route: String) {

    // Auth
    object Login      : NavItem("login")
    object Register   : NavItem("register")

    // Bottom Nav
    object Dashboard  : NavItem("dashboard")
    object Movements  : NavItem("movements")
    object Alerts     : NavItem("alerts")
    object Profile    : NavItem("profile")

    //Productos
    object ProductList : NavItem("product_list")
    object AddProduct  : NavItem("add_product")

    object EditProduct : NavItem("edit_product/{productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun createRoute(productId: String) = "edit_product/$productId"
    }

    object MovementDetail : NavItem("movements/{productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun createRoute(productId: String) = "movements/$productId"
    }

    //Admin
    object Users : NavItem("users")
}

// Ítems del Bottom Navigation Bar
data class BottomNavItem(
    val navItem: NavItem,
    val label: String,
    val iconRes: Int
)

val bottomNavItems = listOf(
    BottomNavItem(NavItem.Dashboard,  "Home",        R.drawable.ic_home),
    BottomNavItem(NavItem.ProductList,  "Productos", R.drawable.ic_shopping_cart),
    BottomNavItem(NavItem.Alerts,     "Alertas",     R.drawable.ic_notifications),
    BottomNavItem(NavItem.Profile,    "Perfil",      R.drawable.ic_person)
)
