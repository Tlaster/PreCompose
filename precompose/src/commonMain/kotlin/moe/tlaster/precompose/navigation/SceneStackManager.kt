package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaveableStateHolder

@Stable
class SceneStackManager(
    private val stateHolder: SaveableStateHolder,
    private val routeGraph: RouteGraph,
) {
    data class Stack(
        val id: Int,
        val scene: Scene,
    )

    private val _scenes = mutableStateListOf<Stack>()
    val currentScene: Stack?
        get() = _scenes.lastOrNull()
    val canGoBack: Boolean
        get() = _scenes.size > 1

    init {
        navigate(route = routeGraph.initialRoute)
    }

    fun navigate(route: String) {
        val scene = routeGraph.scenes.firstOrNull { it.route == route }
        require(scene != null)
        val stack = Stack(
            id = (_scenes.lastOrNull()?.id ?: 0) + 1,
            scene = scene
        )
        _scenes.add(stack)
    }

    fun goBack() {
        val stack = _scenes.removeLast()
        stateHolder.removeState(stack.id)
    }
}