package moe.tlaster.precompose.molecule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
private fun <T> rememberPresenterState(body: @Composable () -> T): StateFlow<T> {
    @Suppress("UNCHECKED_CAST")
    val viewModel = viewModel(
        modelClass = PresenterViewModel::class,
        keys = listOf(
            currentCompositeKeyHash.toString(36),
        ),
        creator = { PresenterViewModel(body) }
    ) as PresenterViewModel<T>
    return viewModel.state
}

private class EventViewModel<T> : ViewModel() {
    val channel = Channel<T>(capacity = Channel.BUFFERED)
    val pair = channel to channel.consumeAsFlow()
    override fun onCleared() {
        channel.close()
    }
}

@Composable
private fun <E> rememberEvent(): Pair<Channel<E>, Flow<E>> {
    @Suppress("UNCHECKED_CAST")
    val viewModel = viewModel(
        modelClass = EventViewModel::class,
        keys = listOf(
            currentCompositeKeyHash.toString(36),
        ),
        creator = { EventViewModel<E>() }
    ) as EventViewModel<E>
    return viewModel.pair
}

/**
 * Return pair of State and Event Channel, use it in your Compose UI
 * The molecule scope and the Event Channel will be managed by the [ViewModel], so it has the same lifecycle as the [ViewModel]
 *
 * @param body The body of the molecule presenter, the flow parameter is the flow of the event channel
 * @return Pair of State and Event channel
 */
@Composable
fun <T, E> rememberPresenter(
    body: @Composable (flow: Flow<E>) -> T
): Pair<T, Channel<E>> {
    rememberCoroutineScope()
    val (channel, event) = rememberEvent<E>()
    val presenter = rememberPresenterState { body(event) }
    val state by presenter.collectAsState()
    return state to channel
}

/**
 * Return pair of State and Event channel, use it in your Presenter, not Compose UI
 *
 * @param body The body of the molecule presenter, the flow parameter is the flow of the event channel
 * @return Pair of State and Event channel
 */
@Composable
fun <T, E> rememberNestedPresenter(
    body: @Composable (flow: Flow<E>) -> T
): Pair<T, Channel<E>> {
    val channel = remember { Channel<E>(capacity = Channel.BUFFERED) }
    val flow = remember { channel.consumeAsFlow() }
    val presenter = body(flow)
    return presenter to channel
}

/**
 * Helper function to collect the event channel in your Presenter
 *
 * @param body Your event handler
 */
@Composable
fun <T> Flow<T>.collectEvent(
    body: suspend T.() -> Unit,
) {
    LaunchedEffect(Unit) {
        collect {
            body(it)
        }
    }
}
