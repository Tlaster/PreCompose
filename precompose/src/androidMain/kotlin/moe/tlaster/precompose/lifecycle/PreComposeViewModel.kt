package moe.tlaster.precompose.lifecycle

import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import moe.tlaster.precompose.stateholder.StateHolder
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner

internal class PreComposeViewModel :
    androidx.lifecycle.ViewModel(),
    LifecycleOwner,
    BackDispatcherOwner {
    val stateHolder by lazy {
        StateHolder()
    }
    val lifecycleRegistry by lazy {
        LifecycleRegistry()
    }
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val backDispatcher by lazy {
        BackDispatcher()
    }

    val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backDispatcher.onBackPress()
        }

        override fun handleOnBackStarted(backEvent: BackEventCompat) {
            backDispatcher.onBackStarted()
        }

        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
            backDispatcher.onBackProgressed(backEvent.progress)
        }

        override fun handleOnBackCancelled() {
            backDispatcher.onBackCancelled()
        }
    }

    override fun onCleared() {
        lifecycleRegistry.updateState(Lifecycle.State.Destroyed)
    }
}
