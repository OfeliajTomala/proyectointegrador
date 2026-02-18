package app.compose.appoxxo.ui.navigation

sealed class NavItem(val route: String) {

    //Auth
    object Login : NavItem("login")
    object Register : NavItem("register")

    //Principal
    object Dashboard : NavItem("dashboard")

    //Productos
    object ProductList : NavItem("product_list")
    object AddProduct : NavItem("add_product")

    object EditProduct : NavItem("edit_product/{productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun createRoute(productId: String) = "edit_product/$productId"
    }

    //Movimientos
    object Movements : NavItem("movements/{productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun createRoute(productId: String) = "movements/$productId"
    }
}