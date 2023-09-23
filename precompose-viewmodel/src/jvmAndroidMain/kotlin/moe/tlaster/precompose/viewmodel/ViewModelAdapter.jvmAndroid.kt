package moe.tlaster.precompose.viewmodel

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.stateholder.SavedStateHolder
import moe.tlaster.precompose.stateholder.StateHolder

@Composable
/**
 * Returns a [ViewModel] instance that is scoped to the given [StateHolder].
 * @param keys A list of keys that will be used to identify the ViewModel.
 * @param creator A function that will be used to create the ViewModel if it doesn't exist.
 * @return A ViewModel instance.
 */
inline fun <reified T : ViewModel> viewModel(
    keys: List<Any?> = emptyList(),
    noinline creator: (SavedStateHolder) -> T,
): T = viewModel(T::class, keys, creator = creator)
