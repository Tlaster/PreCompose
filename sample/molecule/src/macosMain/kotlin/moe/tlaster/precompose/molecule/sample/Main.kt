package moe.tlaster.precompose.molecule.sample

import moe.tlaster.precompose.PreComposeWindow
import platform.AppKit.NSApp

fun main() {
    PreComposeWindow(
        "PreCompose Molecule Sample",
    ) {
        App()
    }
    NSApp?.run()
}
