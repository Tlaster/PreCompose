package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowScope
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
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@Deprecated(
    message = """
        Use Window directly instead. And make sure wrap your content with PreComposeApp.
        PreComposeWindow will be removed in the future release.
        For migration guide, please refer to https://github.com/Tlaster/PreCompose/releases/tag/1.5.5
    """,
    replaceWith = ReplaceWith("PreComposeWindow"),
)
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
            ProvidePreComposeLocals {
                PreComposeApp {
                    content.invoke(this)
                }
            }
        },
    )
}

val LocalWindow = staticCompositionLocalOf<Window> {
    error("No Window for PreCompose, please use ProvidePreComposeLocals in WindowScope to setup your desktop project")
}

@Composable
fun FrameWindowScope.ProvidePreComposeLocals(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalWindow provides window,
        content = content,
    )
}

@Composable
actual fun PreComposeApp(
    content: @Composable () -> Unit,
) {
    val window = LocalWindow.current
    val scope = remember {
        object : WindowScope {
            override val window: Window get() = window
        }
    }
    with(scope) {
        this.PreComposeApp(content)
    }
}

@Composable
fun WindowScope.PreComposeApp(
    content: @Composable () -> Unit,
) {
    val holder = remember {
        PreComposeWindowHolder()
    }
    val listener = remember {
        object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent?) {
                holder.lifecycle.updateState(Lifecycle.State.Active)
            }
            override fun windowClosed(e: WindowEvent?) {
                holder.lifecycle.updateState(Lifecycle.State.Destroyed)
            }
            override fun windowStateChanged(e: WindowEvent?) {
                when (e?.newState) {
                    java.awt.Frame.ICONIFIED -> {
                        holder.lifecycle.updateState(Lifecycle.State.InActive)
                    }
                    else -> {
                        holder.lifecycle.updateState(Lifecycle.State.Active)
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
