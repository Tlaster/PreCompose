package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleObserver
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.navigation.route.SceneRoute
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackHandler
import moe.tlaster.precompose.viewmodel.ViewModelStore
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Stable
internal class BackStackManager(
    private val stateHolder: SaveableStateHolder,
    private val routeGraph: RouteGraph,
) : LifecycleObserver, BackHandler {
    // FIXME: 2021/4/1 Temp workaround for deeplink
    private var pendingNavigation: String? = null
    private val _suspendResult = linkedMapOf<BackStackEntry, Continuation<Any?>>()
    var backDispatcher: BackDispatcher? = null
        set(value) {
            field?.unregister(this)
            field = value
            value?.register(this)
        }
    private var stackEntryId = Long.MIN_VALUE
    var lifeCycleOwner: LifecycleOwner? = null
        set(value) {
            field?.lifecycle?.removeObserver(this)
            field = value
            value?.lifecycle?.addObserver(this)
        }
    private var viewModel: NavControllerViewModel? = null
    private val _backStacks = mutableStateListOf<BackStackEntry>()

    internal val backStacks: List<BackStackEntry>
        get() = _backStacks

    internal val currentEntry: BackStackEntry?
        get() = _backStacks.lastOrNull()

    val canGoBack: Boolean
        get() = _backStacks.size > 1

    private val routeParser by lazy {
        RouteParser().apply {
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
                .flatMap { it.first.map { route -> route to it.second } }.forEach {
                    insert(it.first, it.second)
                }
        }
    }

    internal fun setViewModelStore(viewModelStore: ViewModelStore) {
        if (viewModel != NavControllerViewModel.create(viewModelStore)) {
            viewModel = NavControllerViewModel.create(viewModelStore)
        }
    }

    fun navigate(path: String, options: NavOptions? = null) {
        val vm = viewModel ?: run {
            pendingNavigation = path
            return
        }
        val query = path.substringAfter('?', "")
        val routePath = path.substringBefore('?')
        val matchResult = routeParser.find(path = routePath)
        checkNotNull(matchResult) { "RouteStackManager: navigate target $path not found" }
        require(matchResult.route is ComposeRoute) { "RouteStackManager: navigate target $path is not ComposeRoute" }
        if ( // for launchSingleTop
            options != null &&
            matchResult.route is SceneRoute &&
            options.launchSingleTop &&
            _backStacks.any { it.hasRoute(route = matchResult.route.route, path = path) }
        ) {
            _backStacks.firstOrNull { it.hasRoute(route = matchResult.route.route, path = path) }?.let {
                _backStacks.remove(it)
                _backStacks.add(it)
            }
        } else {
            _backStacks.add(
                BackStackEntry(
                    id = stackEntryId++,
                    route = matchResult.route,
                    pathMap = matchResult.pathMap,
                    queryString = query.takeIf { it.isNotEmpty() }?.let {
                        QueryString(it)
                    },
                    viewModel = vm,
                    path = path
                )
            )
        }

        if (options != null && matchResult.route is SceneRoute) {
            val popUpTo = options.popUpTo
            val index = when (popUpTo) {
                PopUpTo.None -> return
                PopUpTo.Prev -> _backStacks.lastIndex - 1
                is PopUpTo.Route -> if (popUpTo.route.isNotEmpty()) {
                    _backStacks.indexOfLast { it.hasRoute(route = popUpTo.route, path = path) }
                } else 0
            }
            if (index != -1 && index != _backStacks.lastIndex) {
                val stacks = _backStacks.subList(
                    if (popUpTo.inclusive) index else index + 1,
                    _backStacks.lastIndex
                ).toList()
                _backStacks.removeAll(stacks)
                stacks.forEach {
                    stateHolder.removeState(it.id)
                    it.destroy()
                }
            }
        }
    }

    fun goBack(result: Any? = null) {
        if (!canGoBack) {
            backDispatcher?.onBackPress()
            return
        }
        _backStacks.removeLastOrNull()?.apply {
            stateHolder.removeState(id)
            destroy()
        }?.takeIf { backStackEntry ->
            _suspendResult.containsKey(backStackEntry)
        }?.let {
            _suspendResult.remove(it)?.resume(result)
        }
    }

    suspend fun waitingForResult(entry: BackStackEntry): Any? = suspendCoroutine {
        _suspendResult[entry] = it
    }

    override fun onStateChanged(state: Lifecycle.State) {
        when (state) {
            Lifecycle.State.Initialized -> Unit
            Lifecycle.State.Active -> currentEntry?.active()
            Lifecycle.State.InActive -> currentEntry?.inActive()
            Lifecycle.State.Destroyed -> {
                _backStacks.forEach {
                    it.destroy()
                }
                _backStacks.clear()
            }
        }
    }

    override fun handleBackPress(): Boolean {
        return if (canGoBack) {
            goBack()
            true
        } else {
            false
        }
    }

    fun navigateInitial(initialRoute: String) {
        if (_backStacks.isNotEmpty()) {
            return
        }
        navigate(initialRoute)
        pendingNavigation?.let {
            navigate(it)
        }
    }

    internal fun contains(entry: BackStackEntry): Boolean {
        return _backStacks.contains(entry)
    }
}
