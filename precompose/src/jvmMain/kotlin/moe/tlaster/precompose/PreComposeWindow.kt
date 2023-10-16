package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

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
    Window(
        onCloseRequest = onCloseRequest,
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
            PreComposeApp {
                content.invoke(this)
            }
        },
    )
}

@Suppress("INVISIBLE_MEMBER")
@Composable
actual fun PreComposeApp(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val holder = remember {
        PreComposeWindowHolder()
    }
    val window = androidx.compose.ui.window.LocalWindow.current
    if (window != null) {
        val listener = remember {
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    holder.lifecycle.currentState = Lifecycle.State.Destroyed
                }
                override fun windowStateChanged(e: WindowEvent?) {
                    when (e?.newState) {
                        java.awt.Frame.ICONIFIED -> {
                            holder.lifecycle.currentState = Lifecycle.State.InActive
                        }
                        else -> {
                            holder.lifecycle.currentState = Lifecycle.State.Active
                        }
                    }
                }
            }
        }
        DisposableEffect(window) {
            window.addWindowListener(listener)
            window.addWindowStateListener(listener)
            onDispose {
                window.removeWindowListener(listener)
                window.removeWindowStateListener(listener)
            }
        }
    }
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
}
