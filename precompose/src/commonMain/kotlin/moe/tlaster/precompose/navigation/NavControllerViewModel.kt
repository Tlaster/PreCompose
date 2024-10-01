package moe.tlaster.precompose.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

internal class NavControllerViewModel : ViewModel(), ViewModelStoreProvider {
    private val viewModelStores = mutableMapOf<String, ViewModelStore>()

    override fun clear(backStackEntryId: String) {
        // Clear and remove the NavGraph's ViewModelStore
        val viewModelStore = viewModelStores.remove(backStackEntryId)
        viewModelStore?.clear()
    }

    override fun onCleared() {
        for (store in viewModelStores.values) {
            store.clear()
        }
        viewModelStores.clear()
    }

    override fun getViewModelStore(backStackEntryId: String): ViewModelStore {
        var viewModelStore = viewModelStores[backStackEntryId]
        if (viewModelStore == null) {
            viewModelStore = ViewModelStore()
            viewModelStores[backStackEntryId] = viewModelStore
        }
        return viewModelStore
    }

    companion object {
        private val FACTORY: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: KClass<T>,
                    extras: CreationExtras,
                ): T {
                    return NavControllerViewModel() as T
                }
            }

        fun getInstance(viewModelStore: ViewModelStore): NavControllerViewModel {
            val viewModelProvider = ViewModelProvider.create(viewModelStore, FACTORY)
            return viewModelProvider.get()
        }
    }
}
