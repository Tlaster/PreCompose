package moe.tlaster.precompose.navigation

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.cache
import androidx.compose.runtime.currentComposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import moe.tlaster.precompose.navigation.route.ComposeRoute

class BackStackEntry internal constructor(
    val id: Long,
    val route: ComposeRoute,
    val pathMap: Map<String, String>,
    val queryString: QueryString? = null,
    // internal val viewModel: NavControllerViewModel,
) : CoroutineScope by MainScope() {

    // private val recomposer = Recomposer(coroutineContext)
    // private val composition = Composition(UnitApplier, recomposer)
    //
    // init {
    //     composition.setContent {
    //         currentComposer.cache()
    //     }
    // }

    // @Composable
    // inline fun <T> remember(calculation: @DisallowComposableCalls () -> T): T {
    //     // return composition
    // }

    // private val stateFlowMap = mutableMapOf<Any, StateFlow<*>>()
    //
    // fun <T : Any> rememberStateFlow(key: Any, block: CoroutineScope.() -> StateFlow<T>): StateFlow<T> {
    //     @Suppress("UNCHECKED_CAST")
    //     return stateFlowMap.getOrPut(key) {
    //         block(this)
    //     } as StateFlow<T>
    // }
    //
    // inline fun <reified T : Any> rememberStateFlow(noinline block: CoroutineScope.() -> StateFlow<T>): StateFlow<T> {
    //     return rememberStateFlow(T::class, block)
    // }

    // fun <T> getStateFlow(): StateFlow<T> =

    // private var destroyAfterTransition = false

    // override val viewModelStore: ViewModelStore
    //     get() = viewModel.get(id = id)
    //
    // private val lifecycleRegistry by lazy {
    //     LifecycleRegistry()
    // }
    //
    // override val lifecycle: Lifecycle
    //     get() = lifecycleRegistry
    //
    // fun active() {
    //     lifecycleRegistry.currentState = Lifecycle.State.Active
    // }
    //
    // fun inActive() {
    //     lifecycleRegistry.currentState = Lifecycle.State.InActive
    //     if (destroyAfterTransition) {
    //         destroy()
    //     }
    // }

    fun destroy() {
        // if (lifecycleRegistry.currentState != Lifecycle.State.InActive) {
        //     destroyAfterTransition = true
        // } else {
        //     lifecycleRegistry.currentState = Lifecycle.State.Destroyed
        //     viewModelStore.clear()
        // }
        cancel()
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

private object UnitApplier : AbstractApplier<Unit>(Unit) {
    override fun insertBottomUp(index: Int, instance: Unit) {}
    override fun insertTopDown(index: Int, instance: Unit) {}
    override fun move(from: Int, to: Int, count: Int) {}
    override fun remove(index: Int, count: Int) {}
    override fun onClear() {}
}
