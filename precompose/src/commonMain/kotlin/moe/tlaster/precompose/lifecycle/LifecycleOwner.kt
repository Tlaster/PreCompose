package moe.tlaster.precompose.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

interface LifecycleOwner {
    val lifecycle: Lifecycle
}

val LocalLifecycleOwner = compositionLocalOf<LifecycleOwner> { noLocalProvidedFor("LocalLifecycleOwner") }

/**
 * Returns current composition local value for the owner.
 * @throws IllegalStateException if no value was provided.
 */
val currentLocalLifecycleOwner: LifecycleOwner
    @Composable
    @ReadOnlyComposable
    get() = LocalLifecycleOwner.current

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}