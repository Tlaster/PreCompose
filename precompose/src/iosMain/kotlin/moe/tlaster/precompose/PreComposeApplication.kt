package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionName")
fun PreComposeApplication(
    configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
    content: @Composable () -> Unit,
): UIViewController {
    return ComposeUIViewController(configure) {
        PreComposeApp {
            content.invoke()
        }
    }
}
