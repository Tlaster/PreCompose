@file:OptIn(ExperimentalMaterialApi::class)

package moe.tlaster.precompose.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.material.ThresholdConfig
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.navigation.transition.NavTransition

/**
 * @param slideInHorizontally a lambda that takes the full width of the
 * content in pixels and returns the initial offset for the slide-in prev entry,
 * by default it returns -fullWidth/4.
 * For the best impression, lambda should return the save value as [NavTransition.pauseTransition]
 * @param spaceToSwipe width of the swipe space from the left corner of screen
 * @param swipeThreshold amount of offset to perform back navigation
 * @param drawShadow draw shadow on top of previous sliding entry
 * */
class SwipeProperties(
    val slideInHorizontally: (fullWidth: Int) -> Int = { -it / 4 },
    val spaceToSwipe: Dp = 10.dp,
    val swipeThreshold: ThresholdConfig = FixedThreshold(56.dp),
    val drawShadow: Boolean = true
)
