package moe.tlaster.precompose.ui

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

val LocalBackDispatcherOwner = compositionLocalOf<BackDispatcherOwner?> { null }

interface BackDispatcherOwner {
    val backDispatcher: BackDispatcher
}

class BackDispatcher {
    // internal for testing
    internal val handlers = arrayListOf<BackHandler>()
    private var inProgressHandler: BackHandler? = null

    fun onBackPress() {
        val handler = currentHandler()
        inProgressHandler = null
        handler?.handleBackPress()
    }

    fun onBackProgressed(progress: Float) {
        currentHandler()?.handleBackProgressed(progress)
    }

    fun onBackCancelled() {
        val handler = currentHandler()
        inProgressHandler = null
        handler?.handleBackCancelled()
    }

    fun onBackStarted() {
        val handler = handlers.lastOrNull {
            it.isEnabled
        }
        inProgressHandler = handler
        handler?.handleBackStarted()
    }

    private fun currentHandler(): BackHandler? {
        return inProgressHandler ?: handlers.lastOrNull {
            it.isEnabled
        }
    }

    private val canHandleBackPressFlow = MutableStateFlow(0)
    val canHandleBackPress: Flow<Boolean> = canHandleBackPressFlow.map {
        handlers.any { it.isEnabled }
    }

    internal fun onBackStackChanged() {
        canHandleBackPressFlow.value++
    }

    internal fun register(handler: BackHandler) {
        handlers.add(handler)
        onBackStackChanged()
    }

    internal fun unregister(handler: BackHandler) {
        handlers.remove(handler)
        onBackStackChanged()
    }
}

interface BackHandler {
    val isEnabled: Boolean
    fun handleBackPress()
    fun handleBackProgressed(progress: Float)
    fun handleBackCancelled()
    fun handleBackStarted()
}

internal class DefaultBackHandler(
    override var isEnabled: Boolean = true,
    private val onBackPress: () -> Unit,
) : BackHandler {
    override fun handleBackPress() {
        onBackPress()
    }

    override fun handleBackCancelled() {
    }

    override fun handleBackStarted() {
    }

    override fun handleBackProgressed(progress: Float) {
    }
}
