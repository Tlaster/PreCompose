package moe.tlaster.precompose.molecule.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "molecule-sample",
    ) {
        App()
    }
}
