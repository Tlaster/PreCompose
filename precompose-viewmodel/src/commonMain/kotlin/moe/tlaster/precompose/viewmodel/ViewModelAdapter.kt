package moe.tlaster.precompose.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
import kotlin.reflect.KClass

@Composable
/**
 * Returns a [ViewModel] instance that is scoped to the given [ViewModelStoreOwner].
 * CAUTION: If you're using Kotlin/Native target, please use [viewModel] with modelClass parameter instead.
 * @param keys A list of keys that will be used to identify the ViewModel.
 */
inline fun <reified T : ViewModel> viewModel(
    keys: List<Any?> = emptyList(),
    noinline creator: () -> T,
): T = viewModel(T::class, keys, creator = creator)

@Composable
fun <T : ViewModel> viewModel(
    modelClass: KClass<T>,
    keys: List<Any?> = emptyList(),
    creator: () -> T,
): T {
    val stateHolder = checkNotNull(LocalStateHolder.current) {
        "Require LocalStateHolder not null for $modelClass"
    }
    return remember(
        modelClass, keys, creator, stateHolder
    ) {
        stateHolder.getViewModel(keys, modelClass = modelClass, creator = creator)
    }
}

private fun <T : ViewModel> StateHolder.getViewModel(
    keys: List<Any?> = emptyList(),
    modelClass: KClass<T>,
    creator: () -> T,
): T {
    val key = (keys.map { it.hashCode().toString() } + modelClass.simpleName).joinToString()
    return this.getOrPut(key) {
        creator()
    }
}
