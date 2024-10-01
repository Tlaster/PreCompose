package moe.tlaster.precompose

import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

@Composable
actual fun PreComposeApp(
    content: @Composable () -> Unit,
) {
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<PreComposeViewModel>()

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val onBackPressedDispatcher = checkNotNull(androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.onBackPressedDispatcher

    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: androidx.lifecycle.LifecycleOwner) {
                super.onCreate(owner)
                onBackPressedDispatcher.addCallback(owner, viewModel.backPressedCallback)
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    val state by viewModel.backDispatcher.canHandleBackPress.collectAsState(false)

    LaunchedEffect(state) {
        viewModel.backPressedCallback.isEnabled = state
    }
    CompositionLocalProvider(
        LocalBackDispatcherOwner provides viewModel,
    ) {
        content.invoke()
    }
}

internal class PreComposeViewModel :
    androidx.lifecycle.ViewModel(),
    BackDispatcherOwner {
    override val backDispatcher by lazy {
        BackDispatcher()
    }

    val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backDispatcher.onBackPress()
        }

        override fun handleOnBackStarted(backEvent: BackEventCompat) {
            backDispatcher.onBackStarted()
        }

        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
            backDispatcher.onBackProgressed(backEvent.progress)
        }

        override fun handleOnBackCancelled() {
            backDispatcher.onBackCancelled()
        }
    }
}
