import androidx.compose.runtime.ExperimentalComposeApi
import moe.tlaster.common.App
import moe.tlaster.common.di.AppModule
import moe.tlaster.precompose.PreComposeWindow
import org.koin.core.context.startKoin
import platform.AppKit.NSApp

@OptIn(ExperimentalComposeApi::class)
fun main() {
    startKoin {
        modules(AppModule.appModule)
    }
    PreComposeWindow(
        "PreComposeSample",
        // onCloseRequest = {
        //     NSApp?.terminate(null)
        // },
    ) {
        App()
    }
    NSApp?.run()
}
