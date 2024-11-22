package moe.tlaster.precompose.navigation

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class TestViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore
        get() = ViewModelStore()
}

class TestLifecycleOwner : LifecycleOwner {
    private val registry by lazy {
        LifecycleRegistry(this)
    }
    override val lifecycle by lazy {
        registry
    }
}
