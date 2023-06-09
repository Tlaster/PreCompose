package moe.tlaster.precompose.navigation.route

internal data class GroupRoute(
    override val route: String,
    val initialRoute: Route,
) : Route {
    fun hasRoute(route: String): Boolean {
        if (this.route == route) {
            return true
        }
        return if (initialRoute is GroupRoute) {
            initialRoute.hasRoute(route)
        } else {
            initialRoute.route == route
        }
    }
}

internal fun GroupRoute.isSceneRoute(): Boolean = if (initialRoute is GroupRoute) {
    initialRoute.isSceneRoute()
} else {
    initialRoute is SceneRoute
}
internal fun Route.isSceneRoute(): Boolean {
    return this is SceneRoute || this is GroupRoute && this.isSceneRoute()
}

internal fun GroupRoute.toSceneRoute(): SceneRoute? = if (initialRoute is GroupRoute) {
    initialRoute.toSceneRoute()
} else {
    initialRoute as? SceneRoute
}

internal fun Route.toSceneRoute(): SceneRoute? {
    return this as? SceneRoute ?: (this as? GroupRoute)?.toSceneRoute()
}

internal fun GroupRoute.isFloatingRoute(): Boolean = if (initialRoute is GroupRoute) {
    initialRoute.isFloatingRoute()
} else {
    initialRoute is FloatingRoute
}
internal fun Route.isFloatingRoute(): Boolean {
    return this is FloatingRoute || this is GroupRoute && this.isFloatingRoute()
}
