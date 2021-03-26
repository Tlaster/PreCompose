package moe.tlaster.precompose.ui

import androidx.compose.runtime.compositionLocalOf
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.viewmodel.ViewModelStoreOwner

val LocalLifecycleOwner = compositionLocalOf<LifecycleOwner?> { null }

val LocalViewModelStoreOwner = compositionLocalOf<ViewModelStoreOwner?> { null }

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
