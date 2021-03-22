package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaveableStateHolder

@Stable
class RouteStackManager(
    private val stateHolder: SaveableStateHolder,
    private val routeGraph: RouteGraph,
) {

    private val _stacks = mutableStateListOf<BackStackEntry>()
    val current: BackStackEntry?
        get() = _stacks.lastOrNull()
    val canGoBack: Boolean
        get() = _stacks.size > 1
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
        val matchResult = routeParser.find(path = path)
        require(matchResult != null)
        require(matchResult.route is ComposeRoute)
        val stack = BackStackEntry(
            id = (_stacks.lastOrNull()?.id ?: 0) + 1,
            route = matchResult.route,
            pathMap = matchResult.pathMap,
        )
        _stacks.add(stack)
    }

    fun goBack() {
        val stack = _stacks.removeLast()
        stateHolder.removeState(stack.id)
    }
}