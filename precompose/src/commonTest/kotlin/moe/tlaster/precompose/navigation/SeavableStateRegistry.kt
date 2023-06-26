package moe.tlaster.precompose.navigation

import androidx.compose.runtime.saveable.SaveableStateRegistry
import moe.tlaster.precompose.stateholder.SavedStateHolder

@Suppress("TestFunctionName")
internal fun TestSavedStateHolder(
    restored: Map<String, List<Any?>>? = null
) = SavedStateHolder(
    key = "key",
    saveableStateRegistry = SaveableStateRegistry(
        restoredValues = mapOf("key" to listOf(restored)),
        canBeSaved = { true }
    )
)
