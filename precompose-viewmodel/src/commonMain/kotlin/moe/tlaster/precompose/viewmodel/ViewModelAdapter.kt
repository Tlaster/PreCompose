package moe.tlaster.precompose.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import moe.tlaster.precompose.stateholder.LocalSavedStateHolder
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.SavedStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
import kotlin.reflect.KClass

/**
 * Returns a [ViewModel] instance that is scoped to the given [StateHolder].
 * @param modelClass The class of the ViewModel.
 * @param keys A list of keys that will be used to identify the ViewModel.
 * @param creator A function that will be used to create the ViewModel if it doesn't exist.
 * @return A ViewModel instance.
 */
@Composable
fun <T : ViewModel> viewModel(
    modelClass: KClass<T>,
    keys: List<Any?> = emptyList(),
    creator: (SavedStateHolder) -> T,
): T {
    val stateHolder = checkNotNull(LocalStateHolder.current) {
        "Require LocalStateHolder not null for $modelClass"
    }
    val savedStateHolder = checkNotNull(LocalSavedStateHolder.current) {
        "Require LocalSavedStateHolder not null"
    }
    return remember(
        modelClass,
        keys,
        creator,
        stateHolder,
        savedStateHolder,
    ) {
        stateHolder.getViewModel(keys, modelClass = modelClass) {
            creator(savedStateHolder)
        }
    }
}

/**
 * Returns a [ViewModel] instance that is scoped to the given [StateHolder],
 * where [T] is the class of the ViewModel.
 * @param keys A list of keys that will be used to identify the ViewModel.
 * @param creator A function that will be used to create the ViewModel if it doesn't exist.
 * @return A ViewModel instance.
 */
@Composable
inline fun <reified T : ViewModel> viewModel(
    keys: List<Any?> = emptyList(),
    noinline creator: @DisallowComposableCalls (SavedStateHolder) -> T,
): T {
    val stateHolder = checkNotNull(LocalStateHolder.current) {
        "Require LocalStateHolder not null for ${T::class}"
    }
    val savedStateHolder = checkNotNull(LocalSavedStateHolder.current) {
        "Require LocalSavedStateHolder not null"
    }
    return remember(
        T::class,
        keys,
        creator,
        stateHolder,
        savedStateHolder,
    ) {
        stateHolder.getViewModel(keys, modelClass = T::class) {
            creator(savedStateHolder)
        }
    }
}

@PublishedApi
internal fun <T : ViewModel> StateHolder.getViewModel(
    keys: List<Any?> = emptyList(),
    modelClass: KClass<T>,
    creator: () -> T,
): T {
    val key = (keys.map { it.hashCode().toString() } + modelClass.simpleName).joinToString()
    return this.getOrPut(key) {
        creator()
    }
}
