package moe.tlaster.precompose.lifecycle

import moe.tlaster.precompose.viewmodel.ViewModelStore

internal class PreComposeViewModel : androidx.lifecycle.ViewModel() {
    val viewModelStore by lazy {
        ViewModelStore()
    }
}
