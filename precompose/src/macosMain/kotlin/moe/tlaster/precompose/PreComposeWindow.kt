package moe.tlaster.precompose

import androidx.compose.runtime.Composable

fun PreComposeWindow(
    title: String,
    hideTitleBar: Boolean = false,
    onCloseRequest: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    // Ugly workaround until Native macOS support window resize and hide title bar.
    ComposeWindow(
        hideTitleBar = hideTitleBar,
        initialTitle = title,
        onCloseRequest = onCloseRequest,
    ).apply {
        setContent {
            PreComposeApp {
                content.invoke()
            }
        }
    }
}
