package moe.tlaster.precompose.viewmodel

class ViewModelStore {
    private val map = hashMapOf<String, ViewModel>()

    fun put(key: String, viewModel: ViewModel) {
        val oldViewModel = map.put(key, viewModel)
        oldViewModel?.clear()
    }

    operator fun get(key: String): ViewModel? {
        return map[key]
    }

    fun keys(): Set<String> {
        return HashSet(map.keys)
    }

    fun clear() {
        for (vm in map.values) {
            vm.clear()
        }
        map.clear()
    }
}
