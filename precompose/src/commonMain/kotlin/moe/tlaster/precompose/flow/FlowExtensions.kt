package moe.tlaster.precompose.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.lifecycle.repeatOnLifecycle
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects values from this [StateFlow] and represents its latest value via [State] in a
 * lifecycle-aware manner.
 */

@Composable
fun <T : R, R> StateFlow<T>.collectAsStateWithLifecycle(
    context: CoroutineContext = EmptyCoroutineContext
): State<R> {
    return collectAsStateWithLifecycle(initial = this.value, context = context)
}

/**
 * Collects values from this [Flow] and represents its latest value via [State] in a
 * lifecycle-aware manner.
 */

@Composable
fun <T : R, R> Flow<T>.collectAsStateWithLifecycle(
    initial: R,
    context: CoroutineContext = EmptyCoroutineContext
): State<R> {
    val lifecycleOwner = checkNotNull(LocalLifecycleOwner.current)
    return collectAsStateWithLifecycle(
        initial = initial,
        lifecycle = lifecycleOwner.lifecycle,
        context = context
    )
}

/**
 * Collects values from this [Flow] and represents its latest value via [State] in a
 * lifecycle-aware manner.
 */

@Composable
fun <T: R, R> Flow<T>.collectAsStateWithLifecycle(
    initial: R,
    lifecycle: Lifecycle,
    context: CoroutineContext = EmptyCoroutineContext
): State<R> {
    return produceState(initial, this, lifecycle, context) {
        lifecycle.repeatOnLifecycle {
            if (context == EmptyCoroutineContext) {
                this@collectAsStateWithLifecycle.collect { this@produceState.value = it }
            } else withContext(context) {
                this@collectAsStateWithLifecycle.collect { this@produceState.value = it }
            }
        }
    }
}

fun <T> Flow<T>.flowWithLifecycle(
    lifecycle: Lifecycle,
): Flow<T> = callbackFlow {
    lifecycle.repeatOnLifecycle {
        this@flowWithLifecycle.collect {
            send(it)
        }
    }
    close()
}
