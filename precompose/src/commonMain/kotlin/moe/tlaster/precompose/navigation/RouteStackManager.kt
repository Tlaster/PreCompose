package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.snapshots.SnapshotStateList

@Stable
class RouteStackManager(
    private val stateHolder: SaveableStateHolder,
    private val routeGraph: RouteGraph,
) {
    @Stable
    internal class Stack(
        val id: Int,
        val scene: BackStackEntry,
        val dialogStack: SnapshotStateList<BackStackEntry> = mutableStateListOf(),
    ) {
        val current: BackStackEntry
            get() = dialogStack.lastOrNull() ?: scene
        val canGoBack: Boolean
            get() = dialogStack.isNotEmpty()

        fun goBack() {
            dialogStack.removeLast()
        }
    }

    private val _backStacks = mutableStateListOf<Stack>()
    internal val currentStack: Stack?
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
                _backStacks.add(
                    Stack(
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
        }
    }
}