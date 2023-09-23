
import moe.tlaster.common.App
import moe.tlaster.precompose.preComposeWindow
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        preComposeWindow(
            title = "Sample",
        ) {
            App()
        }
    }
}
