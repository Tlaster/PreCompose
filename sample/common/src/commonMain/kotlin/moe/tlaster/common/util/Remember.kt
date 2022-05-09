package moe.tlaster.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun <T : Any> rememberInMemory(
    vararg inputs: Any?,
    key: String? = null,
    init: () -> MutableState<T?>
): MutableState<T?> {
    return rememberSaveable(
        inputs = inputs,
        stateSaver = autoMemorySaver(),
        key = key,
        init = init,
    )
}

private fun <T> autoMemorySaver(): Saver<T, Any> =
    @Suppress("UNCHECKED_CAST")
    (AutoMemorySaver as Saver<T, Any>)

private val AutoMemorySaver = Saver<Any?, Any>(
    save = { MemoryStateHolder(it) },
    restore = { (it as? MemoryStateHolder)?.value }
)

expect class MemoryStateHolder(value: Any?) {
    val value: Any?
}
