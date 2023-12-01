package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIApplicationWillTerminateNotification
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@Suppress("FunctionName")
@Deprecated(
    message = """
        Use ComposeUIViewController directly instead. And make sure wrap your content with PreComposeApp.
        PreComposeApplication will be removed in the future release.
        For migration guide, please refer to https://github.com/Tlaster/PreCompose/releases/tag/1.5.5
    """,
    replaceWith = ReplaceWith("ComposeUIViewController"),
)
fun PreComposeApplication(
    configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
    content: @Composable () -> Unit,
): UIViewController {
    return ComposeUIViewController(configure) {
        PreComposeApp {
            content.invoke()
        }
    }
}

@Composable
actual fun PreComposeApp(
    content: @Composable () -> Unit,
) {
    ProvidePreComposeCompositionLocals {
        content.invoke()
    }
}

@Composable
fun ProvidePreComposeCompositionLocals(
    holder: PreComposeWindowHolder = remember {
        PreComposeWindowHolder()
    },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLifecycleOwner provides holder,
        LocalStateHolder provides holder.stateHolder,
        LocalBackDispatcherOwner provides holder,
    ) {
        content.invoke()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class AppStateHolder(
    private val lifecycle: LifecycleRegistry,
) : NSObject() {
    init {
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString("appMovedToForeground:"),
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
        )
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString("appMovedToBackground:"),
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
        )
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString("appWillTerminate:"),
            name = UIApplicationWillTerminateNotification,
            `object` = null,
        )
        lifecycle.currentState = Lifecycle.State.Active
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun appMovedToForeground(notification: NSNotification) {
        lifecycle.currentState = Lifecycle.State.Active
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun appMovedToBackground(notification: NSNotification) {
        lifecycle.currentState = Lifecycle.State.InActive
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun appWillTerminate(notification: NSNotification) {
        lifecycle.currentState = Lifecycle.State.Destroyed
    }
}

class PreComposeWindowHolder : LifecycleOwner, BackDispatcherOwner {
    override val lifecycle by lazy {
        LifecycleRegistry()
    }
    val stateHolder by lazy {
        StateHolder()
    }
    override val backDispatcher by lazy {
        BackDispatcher()
    }
    private val holder = AppStateHolder(lifecycle)
}
