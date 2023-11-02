package moe.tlaster.precompose.molecule.sample

import androidx.compose.ui.window.Window
import platform.AppKit.NSApp

fun main() {
    Window(
        "PreCompose Molecule Sample",
    ) {
        App()
    }
    NSApp?.run()
}
