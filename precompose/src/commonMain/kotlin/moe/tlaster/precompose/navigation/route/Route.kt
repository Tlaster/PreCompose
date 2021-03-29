package moe.tlaster.precompose.navigation.route

internal interface Route {
    val route: String
    val deepLinks: List<String>
    val pathKeys: List<String>
}
