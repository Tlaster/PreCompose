
import androidx.compose.material.ExperimentalMaterialApi
import moe.tlaster.common.App
import moe.tlaster.precompose.PreComposeWindow

@ExperimentalMaterialApi
fun main() = PreComposeWindow(
    title = "PreCompose Sample"
) {
    App()
}
