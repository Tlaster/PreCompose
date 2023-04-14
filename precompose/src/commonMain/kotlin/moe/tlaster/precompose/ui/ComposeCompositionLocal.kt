package moe.tlaster.precompose.ui

import androidx.compose.runtime.compositionLocalOf
import moe.tlaster.precompose.lifecycle.LifecycleOwner

val LocalLifecycleOwner = compositionLocalOf<LifecycleOwner> { noLocalProvidedFor("LocalLifecycleOwner") }
private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
