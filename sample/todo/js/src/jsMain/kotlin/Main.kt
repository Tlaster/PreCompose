import moe.tlaster.common.App
import moe.tlaster.common.setupKoin
import moe.tlaster.precompose.preComposeWindow
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        setupKoin()
        preComposeWindow(
            title = "Sample",
        ) {
            App()
        }
    }
}
