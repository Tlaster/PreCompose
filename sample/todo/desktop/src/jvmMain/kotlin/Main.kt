
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import moe.tlaster.common.App

fun main() {
    application {
        Window(
            title = "PreCompose Sample",
            onCloseRequest = {
                exitApplication()
            },
        ) {
            App()
        }
    }
}
