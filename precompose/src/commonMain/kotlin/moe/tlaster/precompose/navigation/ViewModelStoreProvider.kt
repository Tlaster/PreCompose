package moe.tlaster.precompose.navigation

import androidx.lifecycle.ViewModelStore

interface ViewModelStoreProvider {
    fun getViewModelStore(backStackEntryId: String): ViewModelStore
    fun clear(backStackEntryId: String)
}
