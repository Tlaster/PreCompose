package moe.tlaster.precompose.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import moe.tlaster.precompose.standard.Disposable
import kotlin.coroutines.CoroutineContext

private const val JOB_KEY = "moe.tlaster.precompose.viewmodel.ViewModelCoroutineScope.JOB_KEY"

val ViewModel.viewModelScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = getTag(JOB_KEY)
        if (scope != null) {
            return scope
        }
        return setTagIfAbsent(
            JOB_KEY,
            CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }

internal class CloseableCoroutineScope(context: CoroutineContext) : Disposable, CoroutineScope {
    override val coroutineContext: CoroutineContext = context

    override fun dispose() {
        coroutineContext.cancel()
    }
}
