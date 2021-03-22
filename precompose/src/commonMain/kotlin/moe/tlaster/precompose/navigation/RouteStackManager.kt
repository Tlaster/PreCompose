package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaveableStateHolder

@Stable
class RouteStackManager(
    private val stateHolder: SaveableStateHolder,
    private val routeGraph: RouteGraph,
) {
    data class Stack(
        val id: Int,
        val route: Route,
        val pathMap: Map<String, String>,
    ) {
        fun path(path: String, default: String = ""): String {
            return pathMap[path] ?: default
        }

        inline fun <reified T> path(path: String, default: T): T {
            val value = pathMap[path] ?: return default
            return when(T::class) {
                Int::class -> value.toInt()
                Long::class -> value.toLong()
                String::class -> value
                Boolean::class -> value.toBoolean()
                Float::class -> value.toFloat()
                Double::class -> value.toDouble()
                else -> throw NotImplementedError()
            } as T
        }
    }

    private val _stacks = mutableStateListOf<Stack>()
    val current: Stack?
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
        val routerMatch = routeParser.find(path = path)
        val route = routerMatch?.route
        require(route != null)
        val stack = Stack(
            id = (_stacks.lastOrNull()?.id ?: 0) + 1,
            route = route,
            pathMap = routerMatch.pathMap,
        )
        _stacks.add(stack)
    }

    fun goBack() {
        val stack = _stacks.removeLast()
        stateHolder.removeState(stack.id)
    }
}