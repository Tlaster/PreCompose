package moe.tlaster.precompose.lifecycle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import moe.tlaster.precompose.PreComposeApp

@Deprecated(
    message = """
        Use ComponentActivity directly instead. And make sure wrap your content with PreComposeApp.
        PreComposeActivity will be removed in the future release.
        For migration guide, please refer to https://github.com/Tlaster/PreCompose/releases/tag/1.5.5
    """,
    replaceWith = ReplaceWith("ComponentActivity"),
)
typealias PreComposeActivity = ComponentActivity

@Deprecated(
    message = """
        Use androidx.activity.compose.setContent directly instead. And make sure wrap your content with PreComposeApp.
        PreComposeActivity.setContent will be removed in the future release.
        For migration guide, please refer to https://github.com/Tlaster/PreCompose/releases/tag/1.5.5
    """,
    replaceWith = ReplaceWith("androidx.activity.compose.setContent"),
)
@Suppress("DEPRECATION")
fun PreComposeActivity.setContent(
    parent: CompositionContext? = null,
    content: @Composable () -> Unit,
) {
    setContent(parent) {
        PreComposeApp {
            content.invoke()
        }
    }
}
