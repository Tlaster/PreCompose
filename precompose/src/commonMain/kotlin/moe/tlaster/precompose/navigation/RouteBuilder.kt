package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable

class RouteBuilder(
    private val initialRoute: String,
) {
    private val scenes = mutableListOf<Scene>()

    fun scene(
        route: String,
        arguments: List<String> = emptyList(),
        content: @Composable () -> Unit,
    ) {
        scenes += Scene(
            route = route,
            arguments = arguments,
            content = content,
        )
    }

    fun build() = RouteGraph(initialRoute, scenes.toList())
}