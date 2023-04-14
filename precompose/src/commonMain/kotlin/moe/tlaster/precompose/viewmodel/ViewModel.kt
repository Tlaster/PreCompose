package moe.tlaster.precompose.viewmodel

@OptIn(ExperimentalStdlibApi::class)
abstract class ViewModel : AutoCloseable {
    private var disposed = false
    private val bagOfTags = hashMapOf<String, Any>()

    protected open fun onCleared() {}

    fun clear() {
        disposed = true
        bagOfTags.let {
            for (value in it.values) {
                disposeWithRuntimeException(value)
            }
        }
        onCleared()
    }

    open fun <T> setTagIfAbsent(key: String, newValue: T): T {
        @Suppress("UNCHECKED_CAST")
        return bagOfTags.getOrPut(key) {
            newValue as Any
        }.also {
            if (disposed) {
                disposeWithRuntimeException(it)
            }
        } as T
    }

    open fun <T> getTag(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return bagOfTags[key] as T?
    }

    private fun disposeWithRuntimeException(obj: Any) {
        if (obj is AutoCloseable) {
            obj.close()
        }
    }

    override fun close() {
        clear()
    }
}
