package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable

data class Scene(
    val route: String,
    val arguments: List<String> = emptyList(),
    val content: @Composable () -> Unit,
)