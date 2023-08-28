import moe.tlaster.common.App
import moe.tlaster.common.setupKoin
import moe.tlaster.precompose.PreComposeWindow
import platform.AppKit.NSApp

fun main() {
    setupKoin()
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
