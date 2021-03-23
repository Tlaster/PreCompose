package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.navigation.route.DialogRoute
import moe.tlaster.precompose.navigation.route.SceneRoute

@Stable
class RouteStackManager(
    private val stateHolder: SaveableStateHolder,
    private val routeGraph: RouteGraph,
) {
    private val _backStacks = mutableStateListOf<RouteStack>()
    internal val currentStack: RouteStack?
        get() = _backStacks.lastOrNull()
    val current: BackStackEntry?
        get() = currentStack?.current
    val canGoBack: Boolean
        get() = currentStack?.canGoBack != false || _backStacks.size > 1
    private val routeParser by lazy {
        RouteParser().apply {
            routeGraph.routes.forEach {
                insert(it)
            }
        }
    }

    init {
        navigate(path = routeGraph.initialRoute)
    }

    fun navigate(path: String) {
        val query = path.substringAfter('?', "")
        val routePath = path.substringBefore('?')
        val matchResult = routeParser.find(path = routePath)
        require(matchResult != null)
        require(matchResult.route is ComposeRoute)
        val entry = BackStackEntry(
            route = matchResult.route,
            pathMap = matchResult.pathMap,
            queryString = query.takeIf { it.isNotEmpty() }?.let {
                QueryString(it)
            }
        )
        when (matchResult.route) {
            is SceneRoute -> {
                currentStack?.onInActive()
                _backStacks.add(
                    RouteStack(
                        id = (_backStacks.lastOrNull()?.id ?: 0) + 1,
                        scene = entry,
                    )
                )
            }
            is DialogRoute -> {
                currentStack?.dialogStack?.add(entry)
            }
        }
    }

    fun goBack() {
        if (currentStack?.canGoBack == true) {
            currentStack?.goBack()
        } else {
            val stack = _backStacks.removeLast()
            stateHolder.removeState(stack.id)
            stack.onDestroyed()
        }
    }
}
