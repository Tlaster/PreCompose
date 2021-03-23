package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaveableStateHolder

@Stable
class RouteStackManager(
    private val stateHolder: SaveableStateHolder,
    private val routeGraph: RouteGraph,
) {
    private val _backStacks = mutableStateListOf<BackStackEntry>()
    val current: BackStackEntry?
        get() = _backStacks.lastOrNull()
    val canGoBack: Boolean
        get() = _backStacks.size > 1
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
        val stack = BackStackEntry(
            id = (_backStacks.lastOrNull()?.id ?: 0) + 1,
            route = matchResult.route,
            pathMap = matchResult.pathMap,
            queryString = query.takeIf { it.isNotEmpty() }?.let {
                QueryString(it)
            }
        )
        _backStacks.add(stack)
    }

    fun goBack() {
        val stack = _backStacks.removeLast()
        stateHolder.removeState(stack.id)
    }
}