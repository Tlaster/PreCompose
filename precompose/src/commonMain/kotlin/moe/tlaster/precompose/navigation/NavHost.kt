package moe.tlaster.precompose.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.lifecycle.currentLocalLifecycleOwner
import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.navigation.route.GroupRoute
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.stateholder.LocalSavedStateHolder
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.currentLocalSavedStateHolder
import moe.tlaster.precompose.stateholder.currentLocalStateHolder
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun NavHost(
    navigator: Navigator,
    initialRoute: String,
    modifier: Modifier = Modifier,
    navTransition: NavTransition = remember { NavTransition() },
    swipeProperties: SwipeProperties? = null,
    builder: RouteBuilder.() -> Unit,
) {
    val lifecycleOwner = currentLocalLifecycleOwner
    val stateHolder = currentLocalStateHolder
    val savedStateHolder = currentLocalSavedStateHolder
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
            if (actualSwipeProperties == null) {
                AnimatedContent(sceneEntry, transitionSpec = transitionSpec) { entry ->
                    SideEffect {
                        navigator.stackManager.canNavigate = !transition.isRunning
                    }
                    NavHostContent(composeStateHolder, entry)
                }
            } else {
                var prevWasSwiped by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(currentSceneEntry) {
                    prevWasSwiped = false
                }

                val dismissState = key(sceneEntry) {
                    rememberDismissState()
                }

                LaunchedEffect(
                    dismissState.isDismissed(DismissDirection.StartToEnd),
                    dismissState.isAnimationRunning,
                ) {
                    navigator.stackManager.canNavigate = !dismissState.isAnimationRunning
                    if (dismissState.isDismissed(DismissDirection.StartToEnd) && !dismissState.isAnimationRunning) {
                        prevWasSwiped = true
                        navigator.goBack()
                    }
                }

                val showPrev by remember(dismissState) {
                    derivedStateOf {
                        dismissState.offset.value > 0f
                    }
                }

                val visibleItems = remember(sceneEntry, prevSceneEntry, showPrev) {
                    if (showPrev) {
                        listOfNotNull(sceneEntry, prevSceneEntry)
                    } else {
                        listOfNotNull(sceneEntry)
                    }
                }

                // display visible items using SwipeItem
                visibleItems.forEachIndexed { index, backStackEntry ->
                    val isPrev = remember(index, visibleItems.size) {
                        index == 1 && visibleItems.size > 1
                    }
                    AnimatedContent(
                        backStackEntry,
                        transitionSpec = {
                            if (prevWasSwiped) {
                                EnterTransition.None togetherWith ExitTransition.None
                            } else {
                                transitionSpec()
                            }
                        },
                        modifier = Modifier.zIndex(
                            if (isPrev) {
                                0f
                            } else {
                                1f
                            },
                        ),
                    ) {
                        SideEffect {
                            navigator.stackManager.canNavigate = !transition.isRunning
                        }
                        SwipeItem(
                            dismissState = dismissState,
                            swipeProperties = actualSwipeProperties,
                            isPrev = isPrev,
                            isLast = !canGoBack,
                            enabled = !transition.isRunning,
                        ) {
                            NavHostContent(composeStateHolder, it)
                        }
                    }
                }
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
    isPrev: Boolean,
    isLast: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (enabled) {
        CustomSwipeToDismiss(
            state = if (isPrev) rememberDismissState() else dismissState,
            spaceToSwipe = swipeProperties.spaceToSwipe,
            enabled = !isLast,
            dismissThreshold = swipeProperties.swipeThreshold,
            modifier = modifier,
        ) {
            Box(
                modifier = Modifier
                    .takeIf { isPrev }
                    ?.graphicsLayer {
                        translationX =
                            swipeProperties.slideInHorizontally(size.width.toInt())
                                .toFloat() -
                            swipeProperties.slideInHorizontally(
                                dismissState.offset.value.absoluteValue.toInt(),
                            )
                    }?.drawWithContent {
                        drawContent()
                        drawRect(
                            swipeProperties.shadowColor,
                            alpha = (1f - dismissState.progress.fraction) *
                                swipeProperties.shadowColor.alpha,
                        )
                    }?.pointerInput(0) {
                        // prev entry should not be interactive until fully appeared
                    } ?: Modifier,
            ) {
                content.invoke()
            }
        }
    } else {
        content.invoke()
    }
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
private fun CustomSwipeToDismiss(
    state: DismissState,
    enabled: Boolean = true,
    spaceToSwipe: Dp = 10.dp,
    modifier: Modifier = Modifier,
    dismissThreshold: ThresholdConfig,
    dismissContent: @Composable () -> Unit,
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val anchors = mutableMapOf(
        0f to DismissValue.Default,
        width to DismissValue.DismissedToEnd,
    )

    val shift = with(LocalDensity.current) {
        remember(this, width, spaceToSwipe) {
            (-width + spaceToSwipe.toPx().coerceIn(0f, width)).roundToInt()
        }
    }
    Box(
        modifier = Modifier
            .offset { IntOffset(x = shift, 0) }
            .swipeable(
                state = state,
                anchors = anchors,
                thresholds = { _, _ -> dismissThreshold },
                orientation = Orientation.Horizontal,
                enabled = enabled && state.currentValue == DismissValue.Default,
                reverseDirection = isRtl,
                resistance = ResistanceConfig(
                    basis = width,
                    factorAtMin = SwipeableDefaults.StiffResistanceFactor,
                    factorAtMax = SwipeableDefaults.StandardResistanceFactor,
                ),
            )
            .offset { IntOffset(x = -shift, 0) }
            .graphicsLayer { translationX = state.offset.value },

    ) {
        dismissContent()
    }
}
