@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")
package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeWindow
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import moe.tlaster.precompose.ui.LocalLifecycleOwner
import moe.tlaster.precompose.ui.LocalViewModelStoreOwner
import moe.tlaster.precompose.viewmodel.ViewModelStore
import moe.tlaster.precompose.viewmodel.ViewModelStoreOwner

fun preComposeWindow(
    title: String = "Untitled",
    // We need to pass the ComposeWindow up to the caller for now,
    // since ComposeJS can't handle window resizes itself yet.
    content: @Composable ComposeWindow.() -> Unit,
) {
    // Ugly workaround until ComposeJS is more mature.
    // Eventually this can just be a Window(title, content) call.
    ComposeWindow().apply {
        setTitle(title)

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
        LocalViewModelStoreOwner provides holder,
        LocalBackDispatcherOwner provides holder
    ) {
        content.invoke()
    }
}

private class PreComposeWindowHolder : LifecycleOwner, ViewModelStoreOwner, BackDispatcherOwner {
    override val lifecycle by lazy {
        LifecycleRegistry()
    }
    override val viewModelStore by lazy {
        ViewModelStore()
    }
    override val backDispatcher by lazy {
        BackDispatcher()
    }
}
