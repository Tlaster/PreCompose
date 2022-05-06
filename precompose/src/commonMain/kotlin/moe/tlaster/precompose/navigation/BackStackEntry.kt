package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.viewmodel.ViewModelStore
import moe.tlaster.precompose.viewmodel.ViewModelStoreOwner

class BackStackEntry internal constructor(
    val id: Long,
    val route: ComposeRoute,
    val pathMap: Map<String, String>,
    val queryString: QueryString? = null,
    internal val viewModel: NavControllerViewModel,
) : ViewModelStoreOwner, LifecycleOwner {
    private var destroyAfterTransition = false

    override val viewModelStore: ViewModelStore
        get() = viewModel.get(id = id)

    private val lifecycleRegistry by lazy {
        LifecycleRegistry()
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    fun active() {
        lifecycleRegistry.currentState = Lifecycle.State.Active
    }

    fun inActive() {
        lifecycleRegistry.currentState = Lifecycle.State.InActive
        if (destroyAfterTransition) {
            destroy()
        }
    }

    fun destroy() {
        if (lifecycleRegistry.currentState != Lifecycle.State.InActive) {
            destroyAfterTransition = true
        } else {
            lifecycleRegistry.currentState = Lifecycle.State.Destroyed
            viewModelStore.clear()
        }
    }
}

inline fun <reified T> BackStackEntry.path(path: String, default: T? = null): T? {
    val value = pathMap[path] ?: return default
    return convertValue(value)
}

inline fun <reified T> BackStackEntry.query(name: String, default: T? = null): T? {
    return queryString?.query(name, default)
}

inline fun <reified T> BackStackEntry.queryList(name: String): List<T?> {
    val value = queryString?.map?.get(name) ?: return emptyList()
    return value.map { convertValue(it) }
}

inline fun <reified T> convertValue(value: String): T? {
    return when (T::class) {
        Int::class -> value.toIntOrNull()
        Long::class -> value.toLongOrNull()
        String::class -> value
        Boolean::class -> value.toBooleanStrictOrNull()
        Float::class -> value.toFloatOrNull()
        Double::class -> value.toDoubleOrNull()
        else -> throw NotImplementedError()
    } as T
}
