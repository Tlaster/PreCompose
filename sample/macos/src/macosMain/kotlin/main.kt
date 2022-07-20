import moe.tlaster.common.App
import moe.tlaster.precompose.PreComposeWindow
import platform.AppKit.NSApp
import platform.AppKit.NSApplication

fun main() {
    NSApplication.sharedApplication()
    PreComposeWindow("PreComposeSample") {
        App()
    }
    NSApp?.run()
}
