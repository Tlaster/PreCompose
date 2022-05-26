package moe.tlaster.precompose.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
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
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavHost(
    navigator: Navigator,
    initialRoute: String,
    modifier: Modifier = Modifier,
    navTransition: NavTransition = remember { NavTransition() },
    builder: RouteBuilder.() -> Unit,
) {
    val stateHolder = rememberSaveableStateHolder()
    val manager = remember {
        val graph = RouteBuilder(initialRoute = initialRoute).apply(builder).build()
        RouteStackManager(stateHolder, graph).apply {
            navigator.stackManager = this
        }
    }

    val backDispatcher = LocalBackDispatcherOwner.current?.backDispatcher
    LaunchedEffect(manager, backDispatcher) {
        manager.backDispatcher = backDispatcher
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
                routeStack.stacks.forEach {
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
        it.route.content.invoke(it)
    }
}
