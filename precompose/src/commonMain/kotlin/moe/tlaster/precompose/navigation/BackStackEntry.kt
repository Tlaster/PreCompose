package moe.tlaster.precompose.navigation

import androidx.compose.runtime.AbstractApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import moe.tlaster.precompose.navigation.route.ComposeRoute

class BackStackEntry internal constructor(
    val id: Long,
    val route: ComposeRoute,
    val pathMap: Map<String, String>,
    val queryString: QueryString? = null,
) : CoroutineScope by MainScope() {
    fun destroy() {
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
