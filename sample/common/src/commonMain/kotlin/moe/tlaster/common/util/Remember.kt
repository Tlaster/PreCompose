package moe.tlaster.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun <T : Any> rememberInMemory(
    vararg inputs: Any?,
    key: String? = null,
    init: () -> T
): T = rememberSaveable(
    inputs = inputs,
    saver = autoMemorySaver(),
    key = key,
    init = init,
)

private fun <T> autoMemorySaver(): Saver<T, MemoryStateHolder> =
    @Suppress("UNCHECKED_CAST")
    (AutoMemorySaver as Saver<T, MemoryStateHolder>)

private val AutoMemorySaver = Saver<Any?, MemoryStateHolder>(
    save = { MemoryStateHolder(it) },
    restore = { it.value }
)

expect class MemoryStateHolder(value: Any?) {
    val value: Any?
}
