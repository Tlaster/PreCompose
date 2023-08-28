
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.window.application
import moe.tlaster.common.App
import moe.tlaster.common.setupKoin
import moe.tlaster.precompose.PreComposeWindow

@OptIn(ExperimentalMaterialApi::class)
fun main() {
    setupKoin()
    application {
        PreComposeWindow(
            title = "PreCompose Sample",
            onCloseRequest = {
                exitApplication()
            },
        ) {
            App()
        }
    }
}
