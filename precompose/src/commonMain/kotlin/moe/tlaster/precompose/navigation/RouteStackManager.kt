package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleObserver
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.navigation.route.DialogRoute
import moe.tlaster.precompose.navigation.route.SceneRoute
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackHandler
import moe.tlaster.precompose.viewmodel.ViewModelStore

@Stable
internal class RouteStackManager(
    private val stateHolder: SaveableStateHolder,
    private val routeGraph: RouteGraph,
) : LifecycleObserver, BackHandler {
    var backDispatcher: BackDispatcher? = null
        set(value) {
            field?.unregister(this)
            field = value
            value?.register(this)
        }
    private var stackEntryId = Long.MIN_VALUE
    private var routeStackId = Long.MIN_VALUE
    var lifeCycleOwner: LifecycleOwner? = null
        set(value) {
            field?.lifecycle?.removeObserver(this)
            field = value
            value?.lifecycle?.addObserver(this)
        }
    private var viewModel: NavControllerViewModel? = null
    private val _backStacks = mutableStateListOf<RouteStack>()
    internal val currentStack: RouteStack?
        get() = _backStacks.lastOrNull()
    val canGoBack: Boolean
        get() = currentStack?.canGoBack != false || _backStacks.size > 1
    private val routeParser by lazy {
        RouteParser().apply {
            routeGraph.routes.map { RouteParser.expandOptionalVariables(it.route) to it }
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

    fun navigate(path: String) {
        val query = path.substringAfter('?', "")
        val routePath = path.substringBefore('?')
        val matchResult = routeParser.find(path = routePath)
        checkNotNull(matchResult) { "RouteStackManager: navigate target $path not found" }
        require(matchResult.route is ComposeRoute) { "RouteStackManager: navigate target $path is not ComposeRoute" }
        val vm = viewModel
        checkNotNull(vm)
        val entry = BackStackEntry(
            id = stackEntryId++,
            route = matchResult.route,
            pathMap = matchResult.pathMap,
            queryString = query.takeIf { it.isNotEmpty() }?.let {
                QueryString(it)
            },
            viewModel = vm,
        )
        when (matchResult.route) {
            is SceneRoute -> {
                _backStacks.add(
                    RouteStack(
                        id = routeStackId++,
                        scene = entry,
                        navTransition = matchResult.route.navTransition,
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
        } else if (_backStacks.any()) {
            val stack = _backStacks.removeLast()
            stateHolder.removeState(stack.id)
            stack.onDestroyed()
        }
    }

    override fun onStateChanged(state: Lifecycle.State) {
        when (state) {
            Lifecycle.State.Initialized -> Unit
            Lifecycle.State.Active -> currentStack?.onActive()
            Lifecycle.State.InActive -> currentStack?.onInActive()
            Lifecycle.State.Destroyed -> {
                _backStacks.forEach {
                    it.onDestroyed()
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

    internal fun indexOf(stack: RouteStack): Int {
        return _backStacks.indexOf(stack)
    }
}
