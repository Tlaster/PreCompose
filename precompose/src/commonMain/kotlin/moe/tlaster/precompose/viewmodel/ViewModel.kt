package moe.tlaster.precompose.viewmodel

import moe.tlaster.precompose.standard.IDisposable

abstract class ViewModel {
    private val bagOfTags = hashMapOf<String, Any>()

    protected open fun onCleared() {}

    fun clear() {
        bagOfTags.let {
            for (value in it.values) {
                closeWithRuntimeException(value)
            }
        }
        onCleared()
    }

    open fun <T> setTagIfAbsent(key: String, newValue: T): T {
        @Suppress("UNCHECKED_CAST")
        return bagOfTags.getOrPut(key) {
            newValue as Any
        } as T
    }

    open fun <T> getTag(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return bagOfTags[key] as T?
    }

    private fun closeWithRuntimeException(obj: Any) {
        if (obj is IDisposable) {
            obj.dispose()
        }
    }
}
