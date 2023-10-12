package moe.tlaster.precompose.navigation

data class QueryString(
    private val rawInput: String,
) {
    val map by lazy {
        rawInput
            .substringAfter("?")
            .splitToSequence("&")
            .map { it.split("=") }
            .filter { it.size in 1..2 && it[0].isNotEmpty() }
            .groupBy({ it[0] }, { it.getOrNull(1) })
            .map { it -> it.key to it.value.mapNotNull { it?.takeIf { it.isNotEmpty() } } }
            .toMap()
    }
}

inline fun <reified T> QueryString.query(name: String, default: T? = null): T? {
    val value = map[name]?.firstOrNull() ?: return default
    return convertValue(value)
}

inline fun <reified T> QueryString.queryList(name: String): List<T> {
    val value = map[name] ?: return emptyList()
    return value.mapNotNull { convertValue(it) }
}
