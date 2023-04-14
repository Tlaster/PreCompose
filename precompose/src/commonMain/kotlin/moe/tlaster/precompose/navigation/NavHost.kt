package moe.tlaster.precompose.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.ui.LocalLifecycleOwner

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
    builder: RouteBuilder.() -> Unit,
) {
    val lifecycleOwner = requireNotNull(LocalLifecycleOwner.current)
    val stateHolder = requireNotNull(LocalStateHolder.current)
    val composeStateHolder = rememberSaveableStateHolder()
    // true for assuming that lifecycleOwner, stateHolder and composeStateHolder are not changing during the lifetime of the NavHost
    LaunchedEffect(true) {
        navigator.init(
            initialRoute = initialRoute,
            routeGraph = RouteBuilder(initialRoute).apply(builder).build(),
            stateHolder = stateHolder,
            lifecycleOwner = lifecycleOwner,
        )
    }

    val transitionSpec: AnimatedContentScope<BackStackEntry>.() -> ContentTransform = {
        val actualTransaction = run {
            if (navigator.stackManager.contains(initialState)) targetState else initialState
        }.navTransition ?: navTransition
        if (!navigator.stackManager.contains(initialState)) {
            actualTransaction.resumeTransition.with(actualTransaction.destroyTransition).apply {
                targetContentZIndex = actualTransaction.enterTargetContentZIndex
            }
        } else {
            actualTransaction.createTransition.with(actualTransaction.pauseTransition).apply {
                targetContentZIndex = actualTransaction.exitTargetContentZIndex
            }
        }
    }

    val canGoBack by navigator.stackManager.canGoBack.collectAsState(false)

    BackHandler(canGoBack) {
        navigator.goBack()
    }

    val currentEntry by navigator.stackManager.currentBackStackEntry.collectAsState(null)
    LaunchedEffect(currentEntry) {
        currentEntry?.composeSaveableStateHolder = composeStateHolder
    }

    Box(modifier) {
        val currentSceneEntry by navigator.stackManager.currentSceneBackStackEntry.collectAsState(null)
        currentSceneEntry?.let {
            AnimatedContent(it, transitionSpec = transitionSpec) { entry ->
                NavHostContent(composeStateHolder, entry)
            }
        }
        val currentFloatingEntry by navigator.stackManager.currentFloatingBackStackEntry.collectAsState(null)
        currentFloatingEntry?.let {
            AnimatedContent(it, transitionSpec = transitionSpec) { entry ->
                NavHostContent(composeStateHolder, entry)
            }
        }
    }
}

@Composable
private fun NavHostContent(
    stateHolder: SaveableStateHolder,
    entry: BackStackEntry
) {
    DisposableEffect(entry) {
        entry.active()
        onDispose {
            entry.inActive()
        }
    }
    stateHolder.SaveableStateProvider(entry.stateId) {
        CompositionLocalProvider(
            LocalStateHolder provides entry.stateHolder,
            LocalLifecycleOwner provides entry,
        ) {
            if (entry.route is ComposeRoute) {
                entry.route.content.invoke(entry)
            }
        }
    }
}
