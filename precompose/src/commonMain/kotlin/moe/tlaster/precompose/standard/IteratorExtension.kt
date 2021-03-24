package moe.tlaster.precompose.standard

/**
 * Make a copy of the original list and iterating it
 * which support removing the original list while iterating
 */
internal fun <T> List<T>.copyForEach(action: (T) -> Unit) {
    copy().forEach(action)
}

/**
 * Make a copy of the original list
 */
internal fun <T> List<T>.copy(): List<T> {
    val result = arrayListOf<T>()
    forEach {
        result += it
    }
    return result
}

/**
 * Make a copy of the original map and iterating it
 * which support removing the original map while iterating
 */
internal fun <K, V> Map<K, V>.copyForEach(action: (Map.Entry<K, V>) -> Unit) {
    copy().forEach(action)
}

/**
 * Make a copy of the original map
 */
internal fun <K, V> Map<K, V>.copy(): Map<K, V> {
    val result = hashMapOf<K, V>()
    result.putAll(this)
    return result
}
