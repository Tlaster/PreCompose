package moe.tlaster.precompose.molecule

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal actual fun providePlatformDispatcher(): CoroutineContext = Dispatchers.Main
