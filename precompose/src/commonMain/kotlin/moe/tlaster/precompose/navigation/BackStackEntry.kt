package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.viewmodel.ViewModelStoreOwner

data class BackStackEntry internal constructor(
    val id: Int,
    val route: ComposeRoute,
    val pathMap: Map<String, String>,
    val queryString: QueryString? = null,
    internal val viewModel: NavControllerViewModel,
) : ViewModelStoreOwner {
    fun path(path: String, default: String? = null): String? {
        return pathMap[path] ?: default
    }

    fun query(name: String, default: String? = null): String? {
        return queryString?.query(name, default)
    }

    override val viewModelStore by lazy {
        viewModel.get(id = id)
    }
}

inline fun <reified T> BackStackEntry.path(path: String, default: T?): T? {
    val value = pathMap[path] ?: return default
    return convertValue(value)
}

inline fun <reified T> BackStackEntry.query(name: String, default: T?): T? {
    return queryString?.query(name, default)
}

inline fun <reified T> BackStackEntry.queryList(name: String): List<T> {
    val value = queryString?.map?.get(name) ?: return emptyList()
    return value.map { convertValue(it) }
}

inline fun <reified T> convertValue(value: String): T {
    return when (T::class) {
        Int::class -> value.toInt()
        Long::class -> value.toLong()
        String::class -> value
        Boolean::class -> value.toBoolean()
        Float::class -> value.toFloat()
        Double::class -> value.toDouble()
        else -> throw NotImplementedError()
    } as T
}
