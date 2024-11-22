
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import moe.tlaster.common.App
import moe.tlaster.common.di.AppModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(AppModule.appModule)
    }
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
