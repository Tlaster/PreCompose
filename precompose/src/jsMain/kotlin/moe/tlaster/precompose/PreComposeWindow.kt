@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")
package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeWindow
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

/**
 * Creates a new [ComposeWindow] with the given [title] and [content].
 *
 * This is a temporary workaround until ComposeJS is more mature.
 * Eventually this can just be a Window(title, content) call.
 */
fun preComposeWindow(
    @Suppress("UNUSED_PARAMETER")
    title: String = "Untitled",
    // We need to pass the ComposeWindow up to the caller for now,
    // since ComposeJS can't handle window resizes itself yet.
    content: @Composable ComposeWindow.() -> Unit,
) {
    // Ugly workaround until ComposeJS is more mature.
    // Eventually this can just be a Window(title, content) call.
    ComposeWindow().apply {
        setContent {
            val holder = remember {
                PreComposeWindowHolder()
            }
            ProvideJsCompositionLocals(holder) {
                content.invoke(this)
            }
        }
    }
}

@Composable
private fun ProvideJsCompositionLocals(
    holder: PreComposeWindowHolder = remember {
        PreComposeWindowHolder()
    },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLifecycleOwner provides holder,
        LocalStateHolder provides holder.stateHolder,
        LocalBackDispatcherOwner provides holder
    ) {
        content.invoke()
    }
}

private class PreComposeWindowHolder : LifecycleOwner, BackDispatcherOwner {
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
