package moe.tlaster.precompose.navigation

data class BackStackEntry(
    val id: Int,
    val route: ComposeRoute,
    val pathMap: Map<String, String>,
) {
    fun path(path: String, default: String = ""): String {
        return pathMap[path] ?: default
    }

    inline fun <reified T> path(path: String, default: T): T {
        val value = pathMap[path] ?: return default
        return when(T::class) {
            Int::class -> value.toInt()
            Long::class -> value.toLong()
            String::class -> value
            Boolean::class -> value.toBoolean()
            Float::class -> value.toFloat()
            Double::class -> value.toDouble()
            else -> throw NotImplementedError()
        } as T
    }
}