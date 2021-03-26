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
import moe.tlaster.precompose.viewmodel.ViewModelStore

@Stable
class RouteStackManager(
    private val stateHolder: SaveableStateHolder,
    private val routeGraph: RouteGraph,
) : LifecycleObserver {
    var stackEntry = 0
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
            routeGraph.routes.forEach {
                insert(it)
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
            id = stackEntry++,
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

    override fun onStateChanged(state: Lifecycle.State) {
        println("on state changed $state")
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
}
