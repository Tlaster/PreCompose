package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder

@Composable
fun NavHost(
    navigator: Navigator,
    initialRoute: String,
    builder: RouteBuilder.() -> Unit
) {
    val stateHolder = rememberSaveableStateHolder()
    val manager = remember(
        stateHolder,
        builder,
        navigator,
    ) {
        val graph = RouteBuilder(initialRoute = initialRoute).apply(builder).build()
        SceneStackManager(stateHolder, graph).apply {
            navigator.stackManager = this
        }
    }
    val currentScene = manager.currentScene
    if (currentScene != null) {
        stateHolder.SaveableStateProvider(currentScene.id) {
            currentScene.scene.content.invoke()
        }
    }
}
