package moe.tlaster.precompose.navigation.transition

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.RouteStack

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun FloatingContent(
    stack: RouteStack,
    modifier: Modifier = Modifier,
    dialogTransition: NavTransition = remember { NavTransition() },
    content: @Composable (BackStackEntry) -> Unit
) {
    val items = remember { mutableStateMapOf<BackStackEntry, @Composable () -> Unit>() }
    val targetState = remember(stack.stacks.size) {
        stack.currentEntry
    }
    val transitionState = remember { MutableTransitionState(targetState) }
    val targetChanged = (targetState != transitionState.targetState)
    val previousState = transitionState.targetState
    transitionState.targetState = targetState
    val transition = updateTransition(transitionState, label = "AnimatedDialogRouteTransition")
    if (targetChanged || items.isEmpty()) {
        val actualTransaction =
            run { if (previousState != null && stack.contains(previousState)) targetState else previousState }?.route?.navTransition
                ?: dialogTransition
        items.clear()
        items.putAll(
            (
                stack.stacks + if (previousState != null && stack.contains(previousState)) {
                    emptyList()
                } else {
                    listOfNotNull(previousState)
                }
                ).map { route ->
                route to @Composable {
                    if (stack.stacks.firstOrNull() == route ||
                        stack.contains(route) && route != targetState ||
                        route == targetState && previousState != null && !stack.contains(previousState)
                    ) {
                        content.invoke(route)
                    } else {
                        transition.AnimatedVisibility(
                            { it == route },
                            enter = actualTransaction.createTransition,
                            exit = actualTransaction.destroyTransition,
                        ) {
                            DisposableEffect(this) {
                                onDispose {
                                    items.remove(route)
                                }
                            }
                            content.invoke(route)
                        }
                    }
                }
            }
        )
    }

    Box(modifier) {
        for (key in items.keys) {
            val item = items[key]
            key(key) {
                Box(modifier = Modifier.fillMaxSize()) {
                    item?.invoke()
                }
            }
        }
    }
}
