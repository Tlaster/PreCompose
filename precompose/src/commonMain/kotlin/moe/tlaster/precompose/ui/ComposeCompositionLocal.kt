package moe.tlaster.precompose.ui

import androidx.compose.runtime.compositionLocalOf
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.viewmodel.ViewModelStoreOwner

val LocalLifecycleOwner = compositionLocalOf<LifecycleOwner> { noLocalProvidedFor("LocalLifecycleOwner") }

val LocalViewModelStoreOwner = compositionLocalOf<ViewModelStoreOwner> { noLocalProvidedFor("ViewModelStoreOwner") }

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
