package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PreComposeApp(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
)
