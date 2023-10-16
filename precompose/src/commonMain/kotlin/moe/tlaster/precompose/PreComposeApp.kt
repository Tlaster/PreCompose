package moe.tlaster.precompose

import androidx.compose.runtime.Composable

@Composable
expect fun PreComposeApp(
    content: @Composable () -> Unit = {},
)
