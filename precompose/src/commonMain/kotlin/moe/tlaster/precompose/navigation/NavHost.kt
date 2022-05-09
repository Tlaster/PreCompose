package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import moe.tlaster.precompose.navigation.transition.AnimatedDialogRoute
import moe.tlaster.precompose.navigation.transition.AnimatedRoute
import moe.tlaster.precompose.navigation.transition.DialogTransition
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

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
@Composable
fun NavHost(
    navigator: Navigator,
    initialRoute: String,
    navTransition: NavTransition = remember { NavTransition() },
    dialogTransition: DialogTransition = remember { DialogTransition() },
    builder: RouteBuilder.() -> Unit,
) {
    val stateHolder = rememberSaveableStateHolder()
    val manager = remember {
        val graph = RouteBuilder(initialRoute = initialRoute).apply(builder).build()
        RouteStackManager(stateHolder, graph).apply {
            navigator.stackManager = this
        }
    }

    // val lifecycleOwner = checkNotNull(LocalLifecycleOwner.current) {
    //     "NavHost requires a LifecycleOwner to be provided via LocalLifecycleOwner"
    // }
    // val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
    //     "NavHost requires a ViewModelStoreOwner to be provided via LocalViewModelStoreOwner"
    // }
    val backDispatcher = LocalBackDispatcherOwner.current?.backDispatcher
    DisposableEffect(manager, backDispatcher) {
        // manager.lifeCycleOwner = lifecycleOwner
        // manager.setViewModelStore(viewModelStoreOwner.viewModelStore)
        manager.backDispatcher = backDispatcher
        onDispose {
            // manager.lifeCycleOwner = null
        }
    }

    LaunchedEffect(manager, initialRoute) {
        manager.navigateInitial(initialRoute)
    }
    val currentStack = manager.currentStack
    if (currentStack != null) {
        AnimatedRoute(
            currentStack,
            navTransition = navTransition,
            manager = manager,
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
                // LaunchedEffect(currentEntry) {
                //     currentEntry.active()
                // }
                // DisposableEffect(currentEntry) {
                //     onDispose {
                //         currentEntry.inActive()
                //     }
                // }
            }
            AnimatedDialogRoute(
                stack = routeStack,
                dialogTransition = dialogTransition,
            ) {
                stateHolder.SaveableStateProvider(it.id) {
                    // CompositionLocalProvider(
                    //     LocalViewModelStoreOwner provides it,
                    //     LocalLifecycleOwner provides it,
                    // ) {
                    it.route.content.invoke(it)
                    // }
                }
            }
        }
    }
}
