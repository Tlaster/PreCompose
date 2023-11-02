import androidx.compose.runtime.ExperimentalComposeApi
import moe.tlaster.common.App
import moe.tlaster.precompose.PreComposeWindow
import platform.AppKit.NSApp

@OptIn(ExperimentalComposeApi::class)
fun main() {
    PreComposeWindow(
        "PreComposeSample",
        onCloseRequest = {
            NSApp?.terminate(null)
        },
    ) {
        App()
    }
    NSApp?.run()
}
