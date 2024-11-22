package moe.tlaster.precompose.navigation

import androidx.lifecycle.ViewModelStore

class TestViewModelStoreProvider : ViewModelStoreProvider {
    private val viewModelStoreMap = mutableMapOf<String, ViewModelStore>()

    override fun getViewModelStore(backStackEntryId: String): ViewModelStore {
        return viewModelStoreMap.getOrPut(backStackEntryId) { ViewModelStore() }
    }

    override fun clear(backStackEntryId: String) {
        viewModelStoreMap.remove(backStackEntryId)
    }

    fun contains(backStackEntryId: String): Boolean {
        return viewModelStoreMap.containsKey(backStackEntryId)
    }
}
