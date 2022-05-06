package moe.tlaster.precompose.navigation.transition

import androidx.compose.ui.graphics.GraphicsLayerScope

val fadeCreateTransition: GraphicsLayerScope.(factor: Float) -> Unit = { factor ->
    alpha = factor
}
val fadeDestroyTransition: GraphicsLayerScope.(factor: Float) -> Unit = { factor ->
    alpha = factor
}

data class DialogTransition(
    /**
     * Transition the scene that about to appear for the first time, similar to activity onCreate, factor from 0.0 to 1.0
     */
    val createTransition: GraphicsLayerScope.(factor: Float) -> Unit = fadeCreateTransition,
    /**
     * Transition the scene that about to disappear forever, similar to activity onDestroy, factor from 1.0 to 0.0
     */
    val destroyTransition: GraphicsLayerScope.(factor: Float) -> Unit = fadeDestroyTransition,
)
