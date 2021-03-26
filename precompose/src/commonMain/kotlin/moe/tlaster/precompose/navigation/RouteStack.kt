package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.navigation.transition.NavTransition

@Stable
internal class RouteStack(
    val id: Int,
    val scene: BackStackEntry,
    val dialogStack: SnapshotStateList<BackStackEntry> = mutableStateListOf(),
    val navTransition: NavTransition? = null,
) : LifecycleOwner {
    private val lifecycleRegistry by lazy {
        LifecycleRegistry()
    }

    val canGoBack: Boolean
        get() = dialogStack.isNotEmpty()

    fun goBack() {
        dialogStack.removeLast().apply {
            viewModelStore.clear()
        }
    }

    fun onActive() {
        lifecycleRegistry.currentState = Lifecycle.State.Active
    }

    fun onInActive() {
        lifecycleRegistry.currentState = Lifecycle.State.InActive
    }

    fun onDestroyed() {
        lifecycleRegistry.currentState = Lifecycle.State.Destroyed
        dialogStack.forEach {
            it.viewModelStore.clear()
        }
        scene.viewModelStore.clear()
    }

    override val lifecycle: Lifecycle by lazy {
        lifecycleRegistry
    }
}
