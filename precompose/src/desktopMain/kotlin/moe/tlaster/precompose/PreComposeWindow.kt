package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

@Composable
fun PreComposeWindow(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable FrameWindowScope.() -> Unit,
) {
    val holder = remember {
        PreComposeWindowHolder()
    }
    // LaunchedEffect(Unit) {
    //     snapshotFlow { state.isMinimized }
    //         .distinctUntilChanged()
    //         .collect {
    //             holder.lifecycle.currentState = if (it) {
    //                 Lifecycle.State.InActive
    //             } else {
    //                 Lifecycle.State.Active
    //             }
    //         }
    // }
    ProvideDesktopCompositionLocals(
        holder
    ) {
        Window(
            onCloseRequest = {
                // holder.lifecycle.currentState = Lifecycle.State.Destroyed
                onCloseRequest.invoke()
            },
            state = state,
            visible = visible,
            title = title,
            icon = icon,
            undecorated = undecorated,
            transparent = transparent,
            resizable = resizable,
            enabled = enabled,
            focusable = focusable,
            alwaysOnTop = alwaysOnTop,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            content = {
                content.invoke(this)
            }
        )
    }
}

@Composable
private fun ProvideDesktopCompositionLocals(
    holder: PreComposeWindowHolder = remember {
        PreComposeWindowHolder()
    },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        // LocalLifecycleOwner provides holder,
        // LocalViewModelStoreOwner provides holder,
        LocalBackDispatcherOwner provides holder,
    ) {
        content.invoke()
    }
}

private class PreComposeWindowHolder : BackDispatcherOwner {
    // override val lifecycle by lazy {
    //     LifecycleRegistry()
    // }
    // override val viewModelStore by lazy {
    //     ViewModelStore()
    // }
    override val backDispatcher by lazy {
        BackDispatcher()
    }
}
