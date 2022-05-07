package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import moe.tlaster.precompose.navigation.transition.NavTransition

@Stable
internal class RouteStack(
    val id: Long,
    val stacks: SnapshotStateList<BackStackEntry> = mutableStateListOf(),
    val navTransition: NavTransition? = null,
) {
    private var destroyAfterTransition = false
    val currentEntry: BackStackEntry?
        get() = stacks.lastOrNull()

    private val _currentBackStackEntryFlow: MutableSharedFlow<BackStackEntry> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val currentBackStackEntryFlow: Flow<BackStackEntry> =
        _currentBackStackEntryFlow.asSharedFlow()

    val canGoBack: Boolean
        get() = stacks.size > 1

    fun goBack(): BackStackEntry {
        return stacks.removeLast().also {
            it.destroy()
        }
    }

    fun onActive() {
        // currentEntry?.active()
        currentEntry?.let {
            _currentBackStackEntryFlow.tryEmit(it)
        }
    }

    fun onInActive() {
        // currentEntry?.inActive()
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
}
