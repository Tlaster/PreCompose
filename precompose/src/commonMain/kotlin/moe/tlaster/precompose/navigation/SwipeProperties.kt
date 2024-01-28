@file:OptIn(ExperimentalMaterialApi::class)

package moe.tlaster.precompose.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.material.ThresholdConfig
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @param spaceToSwipe width of the swipe space from the left side of screen.
 * Can be set to [Int.MAX_VALUE].dp to enable full-scene swipe
 * @param swipeThreshold amount of offset to perform back navigation
 * by swipe progress. Use [Color.Transparent] to disable shadow
 * */
@Suppress("DEPRECATION")
class SwipeProperties(
    val spaceToSwipe: Dp = 10.dp,
    val swipeThreshold: ThresholdConfig = FixedThreshold(56.dp),
)
