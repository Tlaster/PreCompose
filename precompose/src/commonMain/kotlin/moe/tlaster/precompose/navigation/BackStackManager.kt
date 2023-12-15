package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleObserver
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.navigation.route.SceneRoute
import moe.tlaster.precompose.navigation.route.isFloatingRoute
import moe.tlaster.precompose.navigation.route.isSceneRoute
import moe.tlaster.precompose.stateholder.SavedStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max

internal const val STACK_SAVED_STATE_KEY = "BackStackManager"

@Stable
internal class BackStackManager : LifecycleObserver {
    private lateinit var _stateHolder: StateHolder
    private lateinit var _savedStateHolder: SavedStateHolder

    // internal for testing
    internal val backStacks = MutableStateFlow(listOf<BackStackEntry>())
    private val _routeParser = RouteParser()
    private val _suspendResult = linkedMapOf<BackStackEntry, Continuation<Any?>>()
    val currentBackStackEntry: Flow<BackStackEntry?>
        get() = backStacks.asSharedFlow().map { it.lastOrNull() }

    val canGoBack: Flow<Boolean>
        get() = backStacks.asSharedFlow().map { it.size > 1 }

    val currentSceneBackStackEntry: Flow<BackStackEntry?>
        get() = backStacks.asSharedFlow().map { it.lastOrNull { it.route.isSceneRoute() } }

    val prevSceneBackStackEntry: Flow<BackStackEntry?>
        get() = backStacks.asSharedFlow().map {
            it.dropLastWhile { !it.route.isSceneRoute() }
                .dropLast(1)
                .lastOrNull { it.route.isSceneRoute() }
        }

    val currentFloatingBackStackEntry: Flow<BackStackEntry?>
        get() = backStacks.asSharedFlow().map { it.lastOrNull { it.route.isFloatingRoute() } }

    var canNavigate by mutableStateOf(true)

    fun init(
        routeGraph: RouteGraph,
        stateHolder: StateHolder,
        savedStateHolder: SavedStateHolder,
        lifecycleOwner: LifecycleOwner,
        persistNavState: Boolean,
    ) {
        _stateHolder = stateHolder
        _savedStateHolder = savedStateHolder
        lifecycleOwner.lifecycle.addObserver(this)

        if (persistNavState) {
            _savedStateHolder.registerProvider(STACK_SAVED_STATE_KEY) {
                backStacks.value.map { backStackEntry -> backStackEntry.path }
            }
        }
        routeGraph.routes
            .map { route ->
                RouteParser.expandOptionalVariables(route.route).let {
                    if (route is SceneRoute) {
                        it + route.deepLinks.flatMap {
                            RouteParser.expandOptionalVariables(it)
                        }
                    } else {
                        it
                    }
                } to route
            }
            .flatMap { it.first.map { route -> route to it.second } }
            .forEach {
                _routeParser.insert(it.first, it.second)
            }

        @Suppress("UNCHECKED_CAST")
        (_savedStateHolder.consumeRestored(STACK_SAVED_STATE_KEY) as? List<String>)?.let { restoredStacks ->
            restoredStacks.forEach {
                push(it)
            }
        } ?: push(routeGraph.initialRoute)
    }

    fun push(path: String, options: NavOptions? = null) {
        if (!canNavigate) {
            return
        }
        val currentBackStacks = backStacks.value
        val query = path.substringAfter('?', "")
        val routePath = path.substringBefore('?')
        val matchResult = _routeParser.find(path = routePath)
        checkNotNull(matchResult) { "RouteStackManager: navigate target $path not found" }
        // require(matchResult.route is ComposeRoute) { "RouteStackManager: navigate target $path is not ComposeRoute" }
        if ( // for launchSingleTop
            options != null &&
            options.launchSingleTop &&
            currentBackStacks.any { it.hasRoute(matchResult.route.route, path, options.includePath) }
        ) {
            currentBackStacks.firstOrNull { it.hasRoute(matchResult.route.route, path, options.includePath) }
                ?.let { entry ->
                    backStacks.value = backStacks.value.filter { it.id != entry.id } + entry
                }
        } else {
            backStacks.value += BackStackEntry(
                id = (backStacks.value.lastOrNull()?.id ?: 0L) + 1,
                route = matchResult.route,
                pathMap = matchResult.pathMap,
                queryString = query.takeIf { it.isNotEmpty() }?.let {
                    QueryString(it)
                },
                path = path,
                parentStateHolder = _stateHolder,
                parentSavedStateHolder = _savedStateHolder,
            )
        }

        if (options != null && options.popUpTo != PopUpTo.None) {
            val backStack = if (options.launchSingleTop) {
                backStacks.value.dropLast(1)
            } else {
                currentBackStacks
            }

            val popUpTo = options.popUpTo
            val index = when (popUpTo) {
                PopUpTo.None -> -1
                PopUpTo.Prev -> backStack.lastIndex - 1
                is PopUpTo.Route -> if (popUpTo.route.isNotEmpty()) {
                    backStack.indexOfLast { it.hasRoute(popUpTo.route, path, options.includePath) }
                } else {
                    0
                }
            }
            if (index != -1) {
                val stacksToDrop = backStack.subList(
                    if (popUpTo.inclusive) index else index + 1,
                    backStack.size,
                )
                backStacks.value -= stacksToDrop
                stacksToDrop.forEach {
                    it.destroy()
                }
            }
        }
    }

    fun pop(result: Any? = null) {
        if (!canNavigate) {
            return
        }
        val currentBackStacks = backStacks.value
        if (currentBackStacks.size > 1) {
            val last = currentBackStacks.last()
            backStacks.value = currentBackStacks.dropLast(1)
            last.destroy()
            _suspendResult.remove(last)?.resume(result)
        }
    }

    fun popWithOptions(
        popUpTo: PopUpTo,
        inclusive: Boolean,
    ) {
        if (!canNavigate) {
            return
        }
        val currentBackStacks = backStacks.value
        if (currentBackStacks.size <= 1) {
            return
        }
        val index = when (popUpTo) {
            PopUpTo.None -> -1
            PopUpTo.Prev -> currentBackStacks.lastIndex - 1
            is PopUpTo.Route -> if (popUpTo.route.isNotEmpty()) {
                currentBackStacks.indexOfLast { it.hasRoute(popUpTo.route, "", false) }
            } else {
                0
            }
        }.let {
            if (inclusive) it else it + 1
        }.let {
            max(it, 0)
        }
        if (index != -1) {
            val stacksToDrop = currentBackStacks.subList(
                index,
                currentBackStacks.size,
            )
            backStacks.value -= stacksToDrop
            stacksToDrop.forEach {
                _suspendResult.remove(it)?.resume(null)
                it.destroy()
            }
        }
    }

    suspend fun pushForResult(path: String, options: NavOptions? = null): Any? {
        return suspendCoroutine { continuation ->
            push(path, options)
            _suspendResult[backStacks.value.last()] = continuation
        }
    }

    override fun onStateChanged(state: Lifecycle.State) {
        when (state) {
            Lifecycle.State.Initialized -> Unit
            Lifecycle.State.Active -> {
                val currentEntry = backStacks.value.lastOrNull()
                currentEntry?.active()
            }

            Lifecycle.State.InActive -> {
                val currentEntry = backStacks.value.lastOrNull()
                currentEntry?.inActive()
            }

            Lifecycle.State.Destroyed -> {
                backStacks.value.forEach {
                    it.destroy()
                }
                backStacks.value = emptyList()
            }
        }
    }

    fun contains(entry: BackStackEntry): Boolean {
        return backStacks.value.contains(entry)
    }
}
