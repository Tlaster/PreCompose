package moe.tlaster.precompose

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.awt.ComposeWindow

val LocalComposeWindow = compositionLocalOf<ComposeWindow> { error("No provide ComposeWindow") }
