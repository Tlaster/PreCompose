package moe.tlaster.precompose.navigation.route

interface Route {
    val route: String
    @Deprecated("store path key in route node in order to match different links in one route")
    val pathKeys: List<String>
}
