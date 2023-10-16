package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.lifecycle.PreComposeViewModel
import moe.tlaster.precompose.stateholder.LocalSavedStateHolder
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.SavedStateHolder
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

@Composable
actual fun PreComposeApp(
    content: @Composable () -> Unit,
) {
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<PreComposeViewModel>()

    val lifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current.lifecycle
    val onBackPressedDispatcher = checkNotNull(androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.onBackPressedDispatcher

    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: androidx.lifecycle.LifecycleOwner) {
                super.onCreate(owner)
                onBackPressedDispatcher.addCallback(owner, viewModel.backPressedCallback)
            }

            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                super.onResume(owner)
                viewModel.lifecycleRegistry.currentState = Lifecycle.State.Active
            }

            override fun onPause(owner: androidx.lifecycle.LifecycleOwner) {
                super.onPause(owner)
                viewModel.lifecycleRegistry.currentState = Lifecycle.State.InActive
            }

            // override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
            //     super.onDestroy(owner)
            //     if (!isChangingConfigurations) {
            //         viewModel.lifecycleRegistry.currentState = Lifecycle.State.Destroyed
            //     }
            // }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    val state by viewModel.backDispatcher.canHandleBackPress.collectAsState(false)

    val saveableStateRegistry = LocalSaveableStateRegistry.current
    val savedStateHolder = remember(saveableStateRegistry) {
        SavedStateHolder(
            "root",
            saveableStateRegistry,
        )
    }

    LaunchedEffect(state) {
        viewModel.backPressedCallback.isEnabled = state
    }
    CompositionLocalProvider(
        LocalLifecycleOwner provides viewModel,
        LocalStateHolder provides viewModel.stateHolder,
        LocalBackDispatcherOwner provides viewModel,
        LocalSavedStateHolder provides savedStateHolder,
    ) {
        content.invoke()
    }
}
