package moe.tlaster.precompose.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.transition.FloatingContent
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import moe.tlaster.precompose.ui.LocalLifecycleOwner
import moe.tlaster.precompose.ui.LocalViewModelStoreOwner

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [RouteBuilder] can be navigated to from
 * the provided [RouteBuilder].
 *
 * The builder passed into this method is [remember]ed. This means that for this NavHost, the
 * contents of the builder cannot be changed.
 *
 * @param navigator the Navigator for this host
 * @param initialRoute the route for the start destination
 * @param navTransition navigation transition for the scenes in this [NavHost]
 * @param builder the builder used to construct the graph
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavHost(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    initialRoute: String,
    navTransition: NavTransition = remember { NavTransition() },
    dialogTransition: NavTransition = remember { NavTransition() },
    builder: RouteBuilder.() -> Unit,
) {
    val stateHolder = rememberSaveableStateHolder()
    val manager = remember {
        val graph = RouteBuilder(initialRoute = initialRoute).apply(builder).build()
        RouteStackManager(stateHolder, graph).apply {
            navigator.stackManager = this
        }
    }

    val lifecycleOwner = checkNotNull(LocalLifecycleOwner.current) {
        "NavHost requires a LifecycleOwner to be provided via LocalLifecycleOwner"
    }
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "NavHost requires a ViewModelStoreOwner to be provided via LocalViewModelStoreOwner"
    }
    val backDispatcher = LocalBackDispatcherOwner.current?.backDispatcher
    DisposableEffect(manager, lifecycleOwner, viewModelStoreOwner, backDispatcher) {
        manager.lifeCycleOwner = lifecycleOwner
        manager.setViewModelStore(viewModelStoreOwner.viewModelStore)
        manager.backDispatcher = backDispatcher
        onDispose {
            manager.lifeCycleOwner = null
        }
    }

    LaunchedEffect(manager, initialRoute) {
        manager.navigateInitial(initialRoute)
    }
    val currentStack = manager.currentStack
    if (currentStack != null) {
        AnimatedContent(
            currentStack,
            modifier = modifier,
            transitionSpec = {
                val actualTransaction =
                    run { if (manager.contains(initialState)) targetState else initialState }.navTransition
                        ?: navTransition
                if (!manager.contains(initialState)) {
                    actualTransaction.resumeTransition with actualTransaction.destroyTransition
                } else {
                    actualTransaction.createTransition with actualTransaction.pauseTransition
                }
            }
        ) { routeStack ->
            LaunchedEffect(routeStack) {
                routeStack.onActive()
            }
            DisposableEffect(routeStack) {
                onDispose {
                    routeStack.onInActive()
                }
            }
            val currentEntry = routeStack.currentEntry
            if (currentEntry != null) {
                LaunchedEffect(currentEntry) {
                    currentEntry.active()
                }
                DisposableEffect(currentEntry) {
                    onDispose {
                        currentEntry.inActive()
                    }
                }
                FloatingContent(
                    routeStack,
                    dialogTransition = dialogTransition,
                ) {
                    NavHostContent(stateHolder, it)
                }
            }
        }
    }
}

@Composable
private fun NavHostContent(
    stateHolder: SaveableStateHolder,
    it: BackStackEntry
) {
    stateHolder.SaveableStateProvider(it.id) {
        CompositionLocalProvider(
            LocalViewModelStoreOwner provides it,
            LocalLifecycleOwner provides it,
        ) {
            it.route.content.invoke(it)
        }
    }
}
