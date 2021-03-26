package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import moe.tlaster.precompose.ui.LocalLifecycleOwner
import moe.tlaster.precompose.ui.LocalViewModelStoreOwner

@Composable
fun NavHost(
    navigator: Navigator,
    initialRoute: String,
    builder: RouteBuilder.() -> Unit
) {
    val stateHolder = rememberSaveableStateHolder()
    val manager = remember(
        stateHolder,
        builder,
        navigator,
    ) {
        val graph = RouteBuilder(initialRoute = initialRoute).apply(builder).build()
        RouteStackManager(stateHolder, graph).apply {
            navigator.stackManager = this
        }
    }

    val lifecycleOwner = checkNotNull(LocalLifecycleOwner.current) {
        "NavHost requires a LifecycleOwner to be provided via LocalLifecycleOwner"
    }
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "NavHost requires a ViewModelStoreOwner to be provided via LocalViewModelStoreOwner"
    }
    val backDispatcher = LocalBackDispatcherOwner.current?.backDispatcher
    DisposableEffect(manager, lifecycleOwner, viewModelStoreOwner, backDispatcher) {
        manager.lifeCycleOwner = lifecycleOwner
        manager.setViewModelStore(viewModelStoreOwner.viewModelStore)
        manager.backDispatcher = backDispatcher
        onDispose {
            manager.lifeCycleOwner = null
        }
    }

    LaunchedEffect(manager, initialRoute) {
        manager.navigate(initialRoute)
    }

    val currentStack = manager.currentStack
    if (currentStack != null) {
        LaunchedEffect(currentStack) {
            currentStack.onActive()
        }
        DisposableEffect(currentStack) {
            onDispose {
                currentStack.onInActive()
            }
        }
        CompositionLocalProvider(
            LocalLifecycleOwner provides currentStack,
        ) {
            stateHolder.SaveableStateProvider(currentStack.id) {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides currentStack.scene
                ) {
                    currentStack.scene.route.content.invoke(currentStack.scene)
                }
                currentStack.dialogStack.forEach {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides it
                    ) {
                        it.route.content.invoke(it)
                    }
                }
            }
        }
    }
}
