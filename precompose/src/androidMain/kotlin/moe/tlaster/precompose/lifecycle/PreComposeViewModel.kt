package moe.tlaster.precompose.lifecycle

import androidx.activity.OnBackPressedCallback
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.viewmodel.ViewModelStore
import moe.tlaster.precompose.viewmodel.ViewModelStoreOwner

internal class PreComposeViewModel : androidx.lifecycle.ViewModel(),
    LifecycleOwner,
    ViewModelStoreOwner,
    BackDispatcherOwner {
    override val viewModelStore by lazy {
        ViewModelStore()
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
    }
}
