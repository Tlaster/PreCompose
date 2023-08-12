package moe.tlaster.precompose.molecule.sample

import androidx.compose.ui.window.application
import moe.tlaster.precompose.PreComposeWindow

fun main() {
    application {
        PreComposeWindow(
            title = "PreCompose Molecule Sample",
            onCloseRequest = ::exitApplication,
        ) {
            App()
        }
    }
}
