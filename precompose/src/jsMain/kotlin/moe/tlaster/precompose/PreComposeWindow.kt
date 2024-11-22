package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

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

@Composable
actual fun PreComposeApp(
    content: @Composable () -> Unit,
) {
    val holder = remember {
        PreComposeWindowHolder()
    }
    DisposableEffect(holder) {
        onDispose {
            holder.viewModelStore.clear()
        }
    }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides holder,
        LocalBackDispatcherOwner provides holder,
    ) {
        content.invoke()
    }
}

class PreComposeWindowHolder : BackDispatcherOwner, ViewModelStoreOwner {
    override val viewModelStore by lazy {
        ViewModelStore()
    }
    override val backDispatcher by lazy {
        BackDispatcher()
    }
}
