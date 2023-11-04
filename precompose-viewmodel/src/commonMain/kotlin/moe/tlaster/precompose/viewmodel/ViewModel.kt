package moe.tlaster.precompose.viewmodel

@OptIn(ExperimentalStdlibApi::class)
abstract class ViewModel : AutoCloseable {
    private var disposed = false
    private val bagOfTags = hashMapOf<String, Any>()
    private val closeables = linkedSetOf<AutoCloseable>()

    constructor()
    constructor(vararg closeables: AutoCloseable) {
        this.closeables.addAll(closeables)
    }

    fun addCloseable(closeable: AutoCloseable) {
        closeables.add(closeable)
    }

    protected open fun onCleared() {}

    fun clear() {
        disposed = true
        bagOfTags.let {
            for (value in it.values) {
                disposeWithRuntimeException(value)
            }
        }
        bagOfTags.clear()
        closeables.let {
            for (value in it) {
                disposeWithRuntimeException(value)
            }
        }
        closeables.clear()
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
