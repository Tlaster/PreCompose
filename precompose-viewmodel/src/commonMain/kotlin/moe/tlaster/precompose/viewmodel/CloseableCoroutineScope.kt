package moe.tlaster.precompose.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

private const val JOB_KEY = "moe.tlaster.precompose.viewmodel.ViewModelCoroutineScope.JOB_KEY"

/**
 * [CoroutineScope] tied to this [ViewModel].
 * This scope will be canceled when ViewModel will be cleared, i.e [ViewModel.onCleared] is called
 *
 * This scope is bound to
 * [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate]
 */
val ViewModel.viewModelScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = getTag(JOB_KEY)
        if (scope != null) {
            return scope
        }
        return setTagIfAbsent(
            JOB_KEY,
            CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )
    }

@OptIn(ExperimentalStdlibApi::class)
internal class CloseableCoroutineScope(context: CoroutineContext) : AutoCloseable, CoroutineScope {
    override val coroutineContext: CoroutineContext = context

    override fun close() {
        coroutineContext.cancel()
    }
}
