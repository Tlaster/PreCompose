package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

@ExperimentalComposeApi
fun PreComposeWindow(
    title: String,
    // hideTitleBar: Boolean = false,
    // onCloseRequest: () -> Unit = {},
    // onMinimizeRequest: () -> Unit = {},
    // onDeminiaturizeRequest: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    // Ugly workaround until Native macOS support window resize and hide title bar.
    Window(
        title = title,
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
