package moe.tlaster.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun <T> rememberIntentFlow(): MutableSharedFlow<T> {
    return remember { MutableSharedFlow(extraBufferCapacity = 1) }
}
