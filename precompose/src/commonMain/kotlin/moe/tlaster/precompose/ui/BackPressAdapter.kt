package moe.tlaster.precompose.ui

import androidx.compose.runtime.compositionLocalOf

val LocalBackDispatcherOwner = compositionLocalOf<BackDispatcherOwner?> { null }

interface BackDispatcherOwner {
    val backDispatcher: BackDispatcher
}

class BackDispatcher {
    private val handlers = arrayListOf<BackHandler>()

    fun onBackPress(): Boolean {
        for (it in handlers) {
            if (it.handleBackPress()) {
                return true
            }
        }
        return false
    }

    internal fun register(handler: BackHandler) {
        handlers.add(0, handler)
    }

    internal fun unregister(handler: BackHandler) {
        handlers.remove(handler)
    }
}

fun interface BackHandler {
    fun handleBackPress(): Boolean
}
