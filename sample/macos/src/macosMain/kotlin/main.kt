import moe.tlaster.common.App
import moe.tlaster.precompose.PreComposeWindow
import platform.AppKit.NSApp

fun main() {
    PreComposeWindow("PreComposeSample") {
        App()
    }
    NSApp?.run()
}
