package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.CanvasBasedWindow
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
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
    ProvidePreComposeCompositionLocals {
        content.invoke()
    }
}

@Composable
fun ProvidePreComposeCompositionLocals(
    holder: PreComposeWindowHolder = remember {
        PreComposeWindowHolder()
    },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLifecycleOwner provides holder,
        LocalStateHolder provides holder.stateHolder,
        LocalBackDispatcherOwner provides holder,
    ) {
        content.invoke()
    }
}

class PreComposeWindowHolder : LifecycleOwner, BackDispatcherOwner {
    override val lifecycle by lazy {
        LifecycleRegistry()
    }
    val stateHolder by lazy {
        StateHolder()
    }
    override val backDispatcher by lazy {
        BackDispatcher()
    }

    init {
        lifecycle.updateState(Lifecycle.State.Active)
    }
}
