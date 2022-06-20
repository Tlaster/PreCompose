package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import moe.tlaster.precompose.navigation.transition.NavTransition

@Stable
internal class RouteStack(
    val id: Long,
    backStackEntry: BackStackEntry,
    val navTransition: NavTransition? = null,
) {

    val stacks: MutableList<BackStackEntry> = mutableStateListOf(backStackEntry)
    private var destroyAfterTransition = false
    val currentEntry: BackStackEntry?
        get() = stacks.lastOrNull()
    val canGoBack: Boolean
        get() = stacks.size > 1

    fun goBack(): BackStackEntry {
        return stacks.removeLast().also {
            it.destroy()
        }
    }

    fun onActive() {
        currentEntry?.active()
    }

    fun onInActive() {
        currentEntry?.inActive()
        if (destroyAfterTransition) {
            onDestroyed()
        }
    }

    fun destroyAfterTransition() {
        destroyAfterTransition = true
    }

    fun onDestroyed() {
        stacks.forEach {
            it.destroy()
        }
        stacks.clear()
    }

    fun hasRoute(route: String): Boolean {
        return stacks.any { it.route.route == route }
    }

    internal fun contains(entry: BackStackEntry): Boolean {
        return this.stacks.contains(entry)
    }
}
