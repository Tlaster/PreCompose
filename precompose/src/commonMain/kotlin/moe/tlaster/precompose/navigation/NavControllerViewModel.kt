package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.ViewModelStore
import moe.tlaster.precompose.viewmodel.getViewModel

internal class NavControllerViewModel : ViewModel() {
    private val viewModelStores = hashMapOf<Long, ViewModelStore>()

    fun clear(id: Long) {
        viewModelStores.remove(id)?.clear()
    }

    operator fun get(id: Long): ViewModelStore {
        return viewModelStores.getOrPut(id) {
            ViewModelStore()
        }
    }

    override fun onCleared() {
        viewModelStores.forEach {
            it.value.clear()
        }
        viewModelStores.clear()
    }

    companion object {
        fun create(viewModelStore: ViewModelStore): NavControllerViewModel {
            return viewModelStore.getViewModel {
                NavControllerViewModel()
            }
        }
    }
}
