
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import moe.tlaster.common.App
import moe.tlaster.common.di.AppModule
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(AppModule.appModule)
    }
    onWasmReady {
        CanvasBasedWindow(
            title = "Sample",
        ) {
            App()
        }
    }
}
