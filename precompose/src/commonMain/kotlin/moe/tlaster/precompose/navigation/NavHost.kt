@file:OptIn(ExperimentalMaterialApi::class)

package moe.tlaster.precompose.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.ThresholdConfig
import androidx.compose.material.rememberDismissState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.stateholder.LocalStateHolder
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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
 * @param swipeProperties properties of swipe back navigation
 * @param builder the builder used to construct the graph
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavHost(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    initialRoute: String,
    navTransition: NavTransition = remember { NavTransition() },
    swipeProperties: SwipeProperties ?= null,
    builder: RouteBuilder.() -> Unit,
) {
    val lifecycleOwner = requireNotNull(LocalLifecycleOwner.current)
    val stateHolder = requireNotNull(LocalStateHolder.current)
    val composeStateHolder = rememberSaveableStateHolder()
    // true for assuming that lifecycleOwner, stateHolder and composeStateHolder are not changing during the lifetime of the NavHost
    LaunchedEffect(true) {
        navigator.init(
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
        val prevSceneEntry by navigator.stackManager.prevSceneBackStackEntry.collectAsState(null)

        if (swipeProperties == null) {
            currentSceneEntry?.let {
                AnimatedContent(it, transitionSpec = transitionSpec) { entry ->
                    NavHostContent(composeStateHolder, entry)
                }
            }
        } else {
            val scope = rememberCoroutineScope()

            var width by remember {
                mutableStateOf(0)
            }

            var prevWasSwiped by remember {
                mutableStateOf(false)
            }

            LaunchedEffect(currentEntry) {
                delay(100)
                prevWasSwiped = false
            }

            val dismissState = key(currentSceneEntry) {
                rememberDismissState {
                    when (it) {
                        DismissValue.DismissedToEnd -> {
                            scope.launch {
                                delay(200)
                                prevWasSwiped = true
                                navigator.goBack()
                            }
                            true
                        }
                        DismissValue.Default -> true
                        DismissValue.DismissedToStart -> {
                            true
                        }
                    }
                }
            }

            currentSceneEntry?.let { entry ->
                AnimatedContent(
                    entry,
                    transitionSpec = {
                        val actualTransaction = run {
                            if (navigator.stackManager.contains(initialState)) targetState else initialState
                        }.navTransition ?: navTransition

                        if (!navigator.stackManager.contains(initialState)) {
                            if (prevWasSwiped)
                                EnterTransition.None with ExitTransition.None
                            else
                                actualTransaction.resumeTransition.with(actualTransaction.destroyTransition)
                                    .apply {
                                        targetContentZIndex =
                                            actualTransaction.enterTargetContentZIndex
                                    }
                        } else {
                            if (prevWasSwiped) {
                                EnterTransition.None with ExitTransition.None
                            } else {
                                actualTransaction.createTransition.with(actualTransaction.pauseTransition)
                                    .apply {
                                        targetContentZIndex =
                                            actualTransaction.exitTargetContentZIndex
                                    }
                            }
                        }
                    }
                ) {

                    val showPrev by remember(dismissState) {
                        derivedStateOf {
                            dismissState.progress.fraction < 1
                        }
                    }

                    CustomSwipeToDismiss(
                        state = dismissState,
                        spaceToSwipe = swipeProperties.spaceToSwipe,
                        enabled = prevSceneEntry != null,
                        background = {
                            if (showPrev && transition.isRunning.not()) {
                                prevSceneEntry?.let { prev ->
                                    Box(modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                (swipeProperties.slideInHorizontally(width)
                                                    + dismissState.offset.value.absoluteValue / 4).roundToInt(),
                                                0
                                            )
                                        }.drawWithContent {
                                            drawContent()
                                            if (swipeProperties.drawShadow) {
                                                drawRect(
                                                    Color.Black,
                                                    alpha = (1f - dismissState.progress.fraction) / 6f
                                                )
                                            }
                                        }.pointerInput(0){
                                            // prev entry should not be interactive until fully appeared
                                        }
                                    ) {
                                        NavHostContent(composeStateHolder, prev)
                                    }
                                }
                            }
                        }
                    ) {

                        Box(
                            modifier = Modifier
                                .onSizeChanged {
                                    width = it.width
                                }
                        ) {
                            NavHostContent(composeStateHolder, it)
                        }
                    }
                }
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

@Composable
@ExperimentalMaterialApi
private fun CustomSwipeToDismiss(
    state: DismissState,
    enabled : Boolean = true,
    spaceToSwipe : Dp = 10.dp,
    modifier: Modifier = Modifier,
    dismissThresholds: (DismissDirection) -> ThresholdConfig = {
        FixedThreshold(DISMISS_THRESHOLD)
    },
    background: @Composable RowScope.() -> Unit,
    dismissContent: @Composable RowScope.() -> Unit
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val anchors = mutableMapOf(
        0f to DismissValue.Default,
        width to DismissValue.DismissedToEnd
    )

    val thresholds = { from: DismissValue, to: DismissValue ->
        dismissThresholds(getDismissDirection(from, to)!!)
    }
    Box {
        Box(
            Modifier
                .fillMaxHeight()
                .width(spaceToSwipe)
                .zIndex(Float.MAX_VALUE)
                .swipeable(
                    state = state,
                    anchors = anchors,
                    thresholds = thresholds,
                    orientation = Orientation.Horizontal,
                    enabled = enabled && state.currentValue == DismissValue.Default,
                    reverseDirection = isRtl,
                    resistance = ResistanceConfig(
                        basis = width,
                        factorAtMin = SwipeableDefaults.StiffResistanceFactor,
                        factorAtMax = SwipeableDefaults.StandardResistanceFactor
                    )
                )
        )
        Row(
            content = background,
            modifier = Modifier.matchParentSize()
        )
        Row(
            content = dismissContent,
            modifier = Modifier.offset { IntOffset(state.offset.value.roundToInt(), 0) }
        )
    }
}

private fun getDismissDirection(from: DismissValue, to: DismissValue): DismissDirection? {
    return when {
        // settled at the default state
        from == to && from == DismissValue.Default -> null
        // has been dismissed to the end
        from == to && from == DismissValue.DismissedToEnd -> DismissDirection.StartToEnd
        // is currently being dismissed to the end
        from == DismissValue.Default && to == DismissValue.DismissedToEnd -> DismissDirection.StartToEnd
        // has been dismissed to the end but is now animated back to default
        from == DismissValue.DismissedToEnd && to == DismissValue.Default -> DismissDirection.StartToEnd
        else -> null
    }
}

private val DISMISS_THRESHOLD = 56.dp
