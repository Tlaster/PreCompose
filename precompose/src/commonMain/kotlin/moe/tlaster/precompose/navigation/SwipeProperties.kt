@file:OptIn(ExperimentalMaterialApi::class)

package moe.tlaster.precompose.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @param spaceToSwipe width of the swipe space from the left side of screen.
 * Can be set to [Int.MAX_VALUE].dp to enable full-scene swipe
 * */
class SwipeProperties(
    val spaceToSwipe: Dp = 10.dp,
    val positionalThreshold: (totalDistance: Float) -> Float = { distance: Float -> distance * 0.5f },
    val velocityThreshold: Density.() -> Float = { 56.dp.toPx() },
)
