package moe.tlaster.precompose.stateholder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.saveable.SaveableStateRegistry

/**
 * Allows components to save and restore their state using the saved instance state mechanism,
 * which can be useful in some platforms such as Android.
 *
 * @param key The key used scope the state in the provided [saveableStateRegistry].
 * @param saveableStateRegistry The parent [SaveableStateRegistry] to use for saving and restoring
 */
@OptIn(ExperimentalStdlibApi::class)
@Suppress("UNCHECKED_CAST")
class SavedStateHolder(
    private val key: String,
    private val saveableStateRegistry: SaveableStateRegistry?,
) : SaveableStateRegistry by SaveableStateRegistry(
    saveableStateRegistry?.consumeRestored(key) as? Map<String, List<Any?>>,
    { saveableStateRegistry?.canBeSaved(it) ?: true },
),
    AutoCloseable {
    private val registryEntry = saveableStateRegistry?.registerProvider(key) {
        performSave()
    }

    fun child(key: String): SavedStateHolder {
        return SavedStateHolder(key, this)
    }

    override fun close() {
        registryEntry?.unregister()
    }
}

val LocalSavedStateHolder = compositionLocalOf {
    // A default implementation for platforms that don't offer a [SaveableStateRegistry]
    SavedStateHolder("root", null)
}
val currentLocalSavedStateHolder: SavedStateHolder
    @Composable
    @ReadOnlyComposable
    get() = LocalSavedStateHolder.current