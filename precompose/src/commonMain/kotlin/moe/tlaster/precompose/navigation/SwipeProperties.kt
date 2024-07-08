package moe.tlaster.precompose.navigation

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

class SwipeProperties(
    val positionalThreshold: (totalDistance: Float) -> Float = { distance: Float -> distance * 0.5f },
    val velocityThreshold: Density.() -> Float = { 56.dp.toPx() },
)
