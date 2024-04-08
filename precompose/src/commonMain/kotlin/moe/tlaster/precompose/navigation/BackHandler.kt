package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.lifecycle.currentLocalLifecycleOwner
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackHandler
import moe.tlaster.precompose.ui.DefaultBackHandler
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    // Safely update the current `onBack` lambda when a new one is provided
    val backDispatcher = checkNotNull(LocalBackDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.backDispatcher
    val currentOnBack by rememberUpdatedState(onBack)
    // Remember in Composition a back callback that calls the `onBack` lambda
    val backCallback = remember { DefaultBackHandler(enabled) { currentOnBack.invoke() } }
    // On every successful composition, update the callback with the `enabled` value
    SideEffect {
        if (backCallback.isEnabled != enabled) {
            backDispatcher.onBackStackChanged()
        }
        backCallback.isEnabled = enabled
    }
    val lifecycleOwner = currentLocalLifecycleOwner
    DisposableEffect(lifecycleOwner, backDispatcher) {
        // Add callback to the backDispatcher
        backDispatcher.register(backCallback)
        // When the effect leaves the Composition, remove the callback
        onDispose {
            backDispatcher.unregister(backCallback)
        }
    }
}

/**
 * An effect for handling predictive system back gestures.
 *
 * Calling this in your composable adds the given lambda to the [BackDispatcher] of the
 * [LocalBackDispatcherOwner]. The lambda passes in a Flow<Float> where each
 * [Float] reflects the progress of current gesture back. The lambda content should
 * follow this structure:
 *
 * ```
 * PredictiveBackHandler { progress: Flow<Float> ->
 *      // code for gesture back started
 *      try {
 *         progress.collect { progress ->
 *              // code for progress
 *         }
 *         // code for completion
 *      } catch (e: CancellationException) {
 *         // code for cancellation
 *      }
 * }
 * ```
 *
 * If this is called by nested composables, if enabled, the inner most composable will consume
 * the call to system back and invoke its lambda. The call will continue to propagate up until it
 * finds an enabled BackHandler.
 *
 * @param enabled if this BackHandler should be enabled, true by default
 * @param onBack the action invoked by back gesture
 */
@Composable
fun PredictiveBackHandler(
    enabled: Boolean = true,
    onBack: suspend (progress: Flow<Float>) -> Unit,
) {
    // ensure we don't re-register callbacks when onBack changes
    val currentOnBack by rememberUpdatedState(onBack)
    val onBackScope = rememberCoroutineScope()

    val backCallback = remember {
        object : BackHandler {
            override var isEnabled: Boolean = enabled
            var onBackInstance: OnBackInstance? = null

            override fun handleBackStarted() {
                // in case the previous onBackInstance was started by a normal back gesture
                // we want to make sure it's still cancelled before we start a predictive
                // back gesture
                onBackInstance?.cancel()
                onBackInstance = OnBackInstance(onBackScope, true, currentOnBack)
            }

            override fun handleBackProgressed(progress: Float) {
                onBackInstance?.send(progress)
            }

            override fun handleBackPress() {
                // handleOnBackPressed could be called by regular back to restart
                // a new back instance. If this is the case (where current back instance
                // was NOT started by handleOnBackStarted) then we need to reset the previous
                // regular back.
                onBackInstance?.apply {
                    if (!isPredictiveBack) {
                        cancel()
                        onBackInstance = null
                    }
                }
                if (onBackInstance == null) {
                    onBackInstance = OnBackInstance(onBackScope, false, currentOnBack)
                }

                // finally, we close the channel to ensure no more events can be sent
                // but let the job complete normally
                onBackInstance?.close()
            }

            override fun handleBackCancelled() {
                // cancel will purge the channel of any sent events that are yet to be received
                onBackInstance?.cancel()
            }
        }
    }

    val backDispatcher = checkNotNull(LocalBackDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.backDispatcher

    SideEffect {
        if (backCallback.isEnabled != enabled) {
            backDispatcher.onBackStackChanged()
        }
        backCallback.isEnabled = enabled
    }
    val lifecycleOwner = currentLocalLifecycleOwner

    DisposableEffect(lifecycleOwner, backDispatcher) {
        backDispatcher.register(backCallback)

        onDispose {
            backDispatcher.unregister(backCallback)
        }
    }
}

private class OnBackInstance(
    scope: CoroutineScope,
    val isPredictiveBack: Boolean,
    onBack: suspend (progress: Flow<Float>) -> Unit,
) {
    val channel = Channel<Float>(capacity = BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND)
    val job = scope.launch {
        onBack(channel.consumeAsFlow())
    }

    fun send(backEvent: Float) = channel.trySend(backEvent)

    // idempotent if invoked more than once
    fun close() = channel.close()

    fun cancel() {
        channel.cancel(CancellationException("onBack cancelled"))
        job.cancel()
    }
}
