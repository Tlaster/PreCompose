package moe.tlaster.precompose.navigation

internal interface Route {
    val route: String
    val pathKeys: List<String>
}