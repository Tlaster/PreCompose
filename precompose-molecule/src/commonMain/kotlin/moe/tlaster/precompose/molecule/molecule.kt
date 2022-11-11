package moe.tlaster.precompose.molecule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import moe.tlaster.precompose.ui.viewModel
import moe.tlaster.precompose.viewmodel.ViewModel
import kotlin.coroutines.CoroutineContext

internal expect fun providePlatformDispatcher(): CoroutineContext

private class PresenterViewModel<T>(
    body: @Composable () -> T,
) : ViewModel() {
    private val dispatcher = providePlatformDispatcher()
    private val clock = if (dispatcher[MonotonicFrameClock] == null) {
        RecompositionClock.Immediate
    } else {
        RecompositionClock.ContextClock
    }
    private val scope = CoroutineScope(dispatcher)
    val state = scope.launchMolecule(clock, body)

    override fun onCleared() {
        scope.cancel()
    }
}

@Composable
private fun <T> rememberPresenterState(
    keys: List<Any?>,
    body: @Composable () -> T,
): StateFlow<T> {
    @Suppress("UNCHECKED_CAST")
    val viewModel = viewModel(
        modelClass = PresenterViewModel::class,
        keys = keys,
        creator = { PresenterViewModel(body) }
    ) as PresenterViewModel<T>
    return viewModel.state
}

private class ActionViewModel<T> : ViewModel() {
    val channel = Channel<T>(Channel.UNLIMITED)
    val pair = channel to channel.consumeAsFlow()
    override fun onCleared() {
        channel.close()
    }
}

@Composable
private fun <E> rememberAction(
    keys: List<Any?>,
): Pair<Channel<E>, Flow<E>> {
    @Suppress("UNCHECKED_CAST")
    val viewModel = viewModel(
        modelClass = ActionViewModel::class,
        keys = keys,
        creator = { ActionViewModel<E>() }
    ) as ActionViewModel<E>
    return viewModel.pair
}

/**
 * Return pair of State and Action Channel, use it in your Compose UI
 * The molecule scope and the Action Channel will be managed by the [ViewModel], so it has the same lifecycle as the [ViewModel]
 *
 * @param keys The keys to use to identify the Presenter
 * @param body The body of the molecule presenter, the flow parameter is the flow of the action channel
 * @return Pair of State and Action channel
 */
@Composable
fun <T, E> rememberPresenter(
    keys: List<Any?> = emptyList(),
    body: @Composable (flow: Flow<E>) -> T
): Pair<T, Channel<E>> {
    val (channel, action) = rememberAction<E>(keys = keys)
    val presenter = rememberPresenterState(keys = keys) { body(action) }
    val state by presenter.collectAsState()
    return state to channel
}

/**
 * Return pair of State and Action Channel, use it in your Compose UI
 * The molecule scope and the Action Channel will be managed by the [ViewModel], so it has the same lifecycle as the [ViewModel]
 *
 * @param body The body of the molecule presenter, the flow parameter is the flow of the action channel
 * @return Pair of State and Action channel
 */
// @Composable
// inline fun <reified T, reified E> rememberPresenter(
//     crossinline body: @Composable (flow: Flow<E>) -> T
// ): Pair<T, Channel<E>> {
//     return rememberPresenter(keys = listOf(T::class, E::class)) {
//         body.invoke(it)
//     }
// }

/**
 * Return pair of State and Action channel, use it in your Presenter, not Compose UI
 *
 * @param body The body of the molecule presenter, the flow parameter is the flow of the action channel
 * @return Pair of State and Action channel
 */
@Composable
fun <T, E> rememberNestedPresenter(
    body: @Composable (flow: Flow<E>) -> T
): Pair<T, Channel<E>> {
    val channel = remember { Channel<E>(Channel.UNLIMITED) }
    val flow = remember { channel.consumeAsFlow() }
    val presenter = body(flow)
    return presenter to channel
}

/**
 * Helper function to collect the action channel in your Presenter
 *
 * @param body Your action handler
 */
@Composable
fun <T> Flow<T>.collectAction(
    body: suspend T.() -> Unit,
) {
    LaunchedEffect(Unit) {
        collect {
            body(it)
        }
    }
}
