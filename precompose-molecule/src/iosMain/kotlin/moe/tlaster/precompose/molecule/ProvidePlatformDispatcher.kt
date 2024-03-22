package moe.tlaster.precompose.molecule

import app.cash.molecule.DisplayLinkClock
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal actual fun providePlatformDispatcher(): CoroutineContext = DisplayLinkClock + Dispatchers.Main
