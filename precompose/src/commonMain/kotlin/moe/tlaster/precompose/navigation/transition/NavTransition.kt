package moe.tlaster.precompose.navigation.transition

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

/**
 * Create a navigation transition
 */
@OptIn(ExperimentalAnimationApi::class)
data class NavTransition(
    /**
     * Transition the scene that about to appear for the first time, similar to activity onCreate
     */
    val createTransition: EnterTransition = fadeIn() + scaleIn(initialScale = 0.9f),
    /**
     * Transition the scene that about to disappear forever, similar to activity onDestroy
     */
    val destroyTransition: ExitTransition = fadeOut() + scaleOut(targetScale = 0.9f),
    /**
     * Transition the scene that will be pushed into back stack, similar to activity onPause
     * Have no effect for floating/dialog route
     */
    val pauseTransition: ExitTransition = fadeOut() + scaleOut(targetScale = 1.1f),
    /**
     * Transition the scene that about to show from the back stack, similar to activity onResume
     * Have no effect for floating/dialog route
     */
    val resumeTransition: EnterTransition = fadeIn() + scaleIn(initialScale = 1.1f),
)
