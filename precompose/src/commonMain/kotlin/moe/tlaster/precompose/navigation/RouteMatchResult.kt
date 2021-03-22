package moe.tlaster.precompose.navigation

internal data class RouteMatchResult(
    val route: Route,
    val pathMap: Map<String, String> = emptyMap(),
)