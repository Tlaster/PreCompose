
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import moe.tlaster.common.App
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

@OptIn(ExperimentalMaterialApi::class)
fun main() {
    application {
        Window(
            title = "PreCompose Sample",
            onCloseRequest = {
                exitApplication()
            },
        ) {
            CompositionLocalProvider(
                LocalBackDispatcherOwner provides BackDispatcherOwner,
            ) {
                App()
            }
        }
    }
}

private object BackDispatcherOwner : BackDispatcherOwner {
    override val backDispatcher: BackDispatcher by lazy(LazyThreadSafetyMode.NONE) {
        BackDispatcher()
    }
}
