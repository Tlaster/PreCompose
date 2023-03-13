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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import com.benasher44.uuid.uuid4
import moe.tlaster.precompose.navigation.route.FloatingRoute
import moe.tlaster.precompose.navigation.route.SceneRoute
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import moe.tlaster.precompose.ui.LocalLifecycleOwner
import moe.tlaster.precompose.ui.LocalViewModelStoreOwner
import moe.tlaster.precompose.viewmodel.getViewModel

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
    val lifecycleOwner = checkNotNull(LocalLifecycleOwner.current) {
        "NavHost requires a LifecycleOwner to be provided via LocalLifecycleOwner"
    }
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "NavHost requires a ViewModelStoreOwner to be provided via LocalViewModelStoreOwner"
    }
    val id by rememberSaveable { mutableStateOf(uuid4().toString()) }

    val stateHolder = rememberSaveableStateHolder()

    val manager = viewModelStoreOwner.viewModelStore.getViewModel(id, NavHostViewModel::class) {
        val graph = RouteBuilder(initialRoute = initialRoute).apply(builder).build()
        val manager = BackStackManager(stateHolder, graph).apply {
            navigator.init(this)
        }
        NavHostViewModel(manager)
    }.manager

    val backDispatcher = LocalBackDispatcherOwner.current?.backDispatcher
    DisposableEffect(manager, lifecycleOwner, viewModelStoreOwner, backDispatcher) {
        manager.lifeCycleOwner = lifecycleOwner
        manager.setViewModelStore(viewModelStoreOwner.viewModelStore)
        manager.backDispatcher = backDispatcher
        onDispose {
            backDispatcher?.unregister(manager)
            manager.lifeCycleOwner = null
        }
    }

    LaunchedEffect(manager, initialRoute) {
        manager.navigateInitial(initialRoute)
    }
    LaunchedEffect(manager.currentEntry) {
        navigator.currentEntryFlow.value = manager.currentEntry
    }

    val transitionSpec: AnimatedContentScope<BackStackEntry>.() -> ContentTransform = {
        val actualTransaction = run {
            if (manager.contains(initialState)) targetState else initialState
        }.navTransition ?: navTransition
        if (!manager.contains(initialState)) {
            actualTransaction.resumeTransition.with(actualTransaction.destroyTransition).apply {
                targetContentZIndex = actualTransaction.enterTargetContentZIndex
            }
        } else {
            actualTransaction.createTransition.with(actualTransaction.pauseTransition).apply {
                targetContentZIndex = actualTransaction.exitTargetContentZIndex
            }
        }
    }

    Box(modifier) {
        val currentSceneEntry by remember(manager) {
            derivedStateOf {
                manager.backStacks.lastOrNull { it.route is SceneRoute }
            }
        }
        currentSceneEntry?.let {
            AnimatedContent(it, transitionSpec = transitionSpec) { entry ->
                NavHostContent(stateHolder, entry)
            }
        }
        val currentFloatingEntry by remember(manager) {
            derivedStateOf {
                manager.backStacks.lastOrNull()?.takeIf { it.route is FloatingRoute }
            }
        }
        currentFloatingEntry?.let {
            AnimatedContent(it, transitionSpec = transitionSpec) { entry ->
                NavHostContent(stateHolder, entry)
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
    stateHolder.SaveableStateProvider(entry.id) {
        CompositionLocalProvider(
            LocalViewModelStoreOwner provides entry,
            LocalLifecycleOwner provides entry,
        ) {
            entry.route.content.invoke(entry)
        }
    }
}
