package moe.tlaster.precompose.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.ThresholdConfig
import androidx.compose.material.rememberDismissState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.navigation.route.GroupRoute
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.stateholder.LocalSavedStateHolder
import moe.tlaster.precompose.stateholder.LocalStateHolder

/**
 * Provides in place in the Compose hierarchy for self-contained navigation to occur.
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
 * @param swipeProperties properties of swipe back navigation
 * @param builder the builder used to construct the graph
 */
@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalTransitionApi::class,
)
@Composable
fun NavHost(
    navigator: Navigator,
    initialRoute: String,
    modifier: Modifier = Modifier,
    navTransition: NavTransition = remember { NavTransition() },
    swipeProperties: SwipeProperties? = null,
    builder: RouteBuilder.() -> Unit,
) {
    val lifecycleOwner = requireNotNull(LocalLifecycleOwner.current)
    val stateHolder = requireNotNull(LocalStateHolder.current)
    val savedStateHolder = requireNotNull(LocalSavedStateHolder.current)
    val composeStateHolder = rememberSaveableStateHolder()

    // true for assuming that lifecycleOwner, stateHolder and composeStateHolder are not changing during the lifetime of the NavHost
    LaunchedEffect(true) {
        navigator.init(
            stateHolder = stateHolder,
            savedStateHolder = savedStateHolder,
            lifecycleOwner = lifecycleOwner,
        )
    }

    LaunchedEffect(builder, initialRoute) {
        navigator.setRouteGraph(
            RouteBuilder(initialRoute).apply(builder).build(),
        )
    }

    val transitionSpec: AnimatedContentTransitionScope<BackStackEntry>.() -> ContentTransform = {
        val actualTransaction = run {
            if (navigator.stackManager.contains(initialState)) targetState else initialState
        }.navTransition ?: navTransition
        if (!navigator.stackManager.contains(initialState)) {
            actualTransaction.resumeTransition.togetherWith(actualTransaction.destroyTransition)
                .apply {
                    targetContentZIndex = actualTransaction.enterTargetContentZIndex
                }
        } else {
            actualTransaction.createTransition.togetherWith(actualTransaction.pauseTransition)
                .apply {
                    targetContentZIndex = actualTransaction.exitTargetContentZIndex
                }
        }
    }

    val canGoBack by navigator.stackManager.canGoBack.collectAsState(false)

    val currentEntry by navigator.stackManager.currentBackStackEntry.collectAsState(null)

    LaunchedEffect(currentEntry, composeStateHolder) {
        val entry = currentEntry
        val route = entry?.route
        if (route is ComposeRoute || route is GroupRoute && route.initialRoute is ComposeRoute) {
            val closable = entry.uiClosable
            if (closable == null || closable !is ComposeUiClosable || closable.composeSaveableStateHolder != composeStateHolder) {
                entry.uiClosable = ComposeUiClosable(composeStateHolder)
            }
        }
    }

    Box(modifier) {
        val currentSceneEntry by navigator.stackManager
            .currentSceneBackStackEntry.collectAsState(null)
        val prevSceneEntry by navigator.stackManager
            .prevSceneBackStackEntry.collectAsState(null)
        BackHandler(canGoBack) {
            navigator.goBack()
        }
        currentSceneEntry?.let { sceneEntry ->
            val actualSwipeProperties = sceneEntry.swipeProperties ?: swipeProperties
            val dismissState = rememberDismissState()
            LaunchedEffect(
                dismissState.isDismissed(DismissDirection.StartToEnd),
                dismissState.isAnimationRunning,
            ) {
                navigator.stackManager.canNavigate = !dismissState.isAnimationRunning
                if (dismissState.isDismissed(DismissDirection.StartToEnd) && !dismissState.isAnimationRunning) {
                    navigator.goBack()
                    dismissState.snapTo(DismissValue.Default)
                }
            }
            val showPrev by remember(dismissState) {
                derivedStateOf {
                    dismissState.offset.value > 0f
                }
            }
            val transition = if (showPrev && prevSceneEntry != null) {
                val transitionState by remember(sceneEntry) {
                    mutableStateOf(SeekableTransitionState(sceneEntry, prevSceneEntry!!))
                }
                LaunchedEffect(dismissState.progress.fraction) {
                    transitionState.snapToFraction(dismissState.progress.fraction)
                }
                rememberTransition(transitionState, label = "entry")
            } else {
                updateTransition(sceneEntry, label = "entry")
            }
            SideEffect {
                navigator.stackManager.canNavigate = !transition.isRunning
            }
            transition.AnimatedContent(
                transitionSpec = transitionSpec,
                contentKey = { it.stateId },
            ) {
                NavHostContent(composeStateHolder, it)
            }
            if (actualSwipeProperties != null) {
                SwipeItem(
                    dismissState = dismissState,
                    swipeProperties = actualSwipeProperties,
                    enabled = prevSceneEntry != null,
                )
            }
        }
        val currentFloatingEntry by navigator.stackManager
            .currentFloatingBackStackEntry.collectAsState(null)
        currentFloatingEntry?.let {
            AnimatedContent(it, transitionSpec = transitionSpec) { entry ->
                NavHostContent(composeStateHolder, entry)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeItem(
    dismissState: DismissState,
    swipeProperties: SwipeProperties,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    CustomSwipeToDismiss(
        state = dismissState,
        spaceToSwipe = swipeProperties.spaceToSwipe,
        enabled = enabled,
        dismissThreshold = swipeProperties.swipeThreshold,
        modifier = modifier,
    )
}

@Composable
private fun NavHostContent(
    stateHolder: SaveableStateHolder,
    entry: BackStackEntry,
) {
    stateHolder.SaveableStateProvider(entry.stateId) {
        CompositionLocalProvider(
            LocalStateHolder provides entry.stateHolder,
            LocalSavedStateHolder provides entry.savedStateHolder,
            LocalLifecycleOwner provides entry,
            content = {
                entry.ComposeContent()
            },
        )
    }
    DisposableEffect(entry) {
        entry.active()
        onDispose {
            entry.inActive()
        }
    }
}

private fun GroupRoute.composeRoute(): ComposeRoute? {
    return if (initialRoute is GroupRoute) {
        initialRoute.composeRoute()
    } else {
        initialRoute as? ComposeRoute
    }
}

@Composable
private fun BackStackEntry.ComposeContent() {
    if (route is GroupRoute) {
        (route as GroupRoute).composeRoute()
    } else {
        route as? ComposeRoute
    }?.content?.invoke(this)
}

@Composable
@ExperimentalMaterialApi
// idk why rememberDismissState is not being deprecated
@Suppress("DEPRECATION")
private fun CustomSwipeToDismiss(
    state: DismissState,
    enabled: Boolean = true,
    spaceToSwipe: Dp = 10.dp,
    modifier: Modifier = Modifier,
    dismissThreshold: ThresholdConfig,
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val anchors = mutableMapOf(
        0f to DismissValue.Default,
        width to DismissValue.DismissedToEnd,
    )
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(spaceToSwipe)
            .swipeable(
                state = state,
                anchors = anchors,
                thresholds = { _, _ -> dismissThreshold },
                orientation = Orientation.Horizontal,
                enabled = enabled,
                reverseDirection = isRtl,
                resistance = ResistanceConfig(
                    basis = width,
                    factorAtMin = SwipeableDefaults.StiffResistanceFactor,
                    factorAtMax = SwipeableDefaults.StandardResistanceFactor,
                ),
            ),

    )
}
