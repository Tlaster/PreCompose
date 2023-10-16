package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.CanvasBasedWindow

/**
 * Creates a new [CanvasBasedWindow] with the given [title] and [content].
 */
@OptIn(ExperimentalComposeUiApi::class)
fun preComposeWindow(
    title: String = "Untitled",
    canvasElementId: String = "ComposeTarget",
    requestResize: (suspend () -> IntSize)? = null,
    applyDefaultStyles: Boolean = true,
    content: @Composable () -> Unit,
) {
    CanvasBasedWindow(
        title = title,
        canvasElementId = canvasElementId,
        requestResize = requestResize,
        applyDefaultStyles = applyDefaultStyles,
        content = {
            PreComposeApp {
                content.invoke()
            }
        },
    )
}
