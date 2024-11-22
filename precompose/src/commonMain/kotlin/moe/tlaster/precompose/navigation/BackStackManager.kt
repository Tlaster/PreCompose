package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import moe.tlaster.precompose.navigation.route.SceneRoute
import moe.tlaster.precompose.navigation.route.isFloatingRoute
import moe.tlaster.precompose.navigation.route.isSceneRoute
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max

@Stable
internal class BackStackManager : LifecycleEventObserver {
    private lateinit var _navControllerViewModel: NavControllerViewModel

    // internal for testing
    internal val backStacks = MutableStateFlow(listOf<BackStackEntry>())
    private var _routeParser = RouteParser()
    private val _suspendResult = linkedMapOf<BackStackEntry, Continuation<Any?>>()
    private var _routeGraph: RouteGraph? = null
        set(value) {
            field = value
            if (value != null) {
                _routeParser = RouteParser()
                value.routes
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
            }
        }
    val currentBackStackEntry: Flow<BackStackEntry?>
        get() = backStacks.asSharedFlow().map { it.lastOrNull() }

    val prevBackStackEntry: Flow<BackStackEntry?>
        get() = backStacks.asSharedFlow().map { it.dropLast(1).lastOrNull() }

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

    fun init(
        lifecycleOwner: LifecycleOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
    ) {
        _navControllerViewModel = NavControllerViewModel.getInstance(viewModelStoreOwner.viewModelStore)
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun setRouteGraph(
        routeGraph: RouteGraph,
    ) {
        if (_routeGraph != routeGraph) {
            _routeGraph?.let {
                // clear all backstacks
                backStacks.value.forEach {
                    it.destroy()
                }
                backStacks.value = emptyList()
            }
            _routeGraph = routeGraph
            // push to initial route
            push(routeGraph.initialRoute)
        } else {
            _routeGraph = routeGraph
            // update routes
            backStacks.value.forEach { entry ->
                entry.routeInternal = _routeParser.find(entry.path)?.route ?: entry.routeInternal
            }
        }
    }

    fun push(path: String, options: NavOptions? = null) {
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
                    backStacks.value = backStacks.value.filter { it.stateId != entry.stateId } + entry
                }
        } else {
            backStacks.value += BackStackEntry(
                stateId = uuid4().toString(),
                routeInternal = matchResult.route,
                pathMap = matchResult.pathMap,
                queryString = query.takeIf { it.isNotEmpty() }?.let {
                    QueryString(it)
                },
                path = path,
                provider = _navControllerViewModel,
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
    ) {
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
            if (popUpTo.inclusive) it else it + 1
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

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                backStacks.value.forEach {
                    it.destroy()
                }
                backStacks.value = emptyList()
            }
            else -> {
                val currentEntry = backStacks.value.lastOrNull()
                currentEntry?.onStateChanged(source, event)
            }
        }
    }

    fun contains(entry: BackStackEntry): Boolean {
        return backStacks.value.contains(entry)
    }
}
