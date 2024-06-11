package moe.tlaster.precompose.molecule.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import moe.tlaster.precompose.ProvidePreComposeLocals

fun main() {
    application {
        Window(
            title = "PreCompose Molecule Sample",
            onCloseRequest = ::exitApplication,
        ) {
            ProvidePreComposeLocals {
                App()
            }
        }
    }
}
