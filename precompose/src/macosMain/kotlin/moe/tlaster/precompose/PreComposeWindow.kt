package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
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
}
