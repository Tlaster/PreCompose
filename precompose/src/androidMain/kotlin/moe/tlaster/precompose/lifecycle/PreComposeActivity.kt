package moe.tlaster.precompose.lifecycle

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import moe.tlaster.precompose.ui.LocalLifecycleOwner
import moe.tlaster.precompose.ui.LocalViewModelStoreOwner
import moe.tlaster.precompose.viewmodel.ViewModelStoreOwner

open class PreComposeActivity :
    ComponentActivity(),
    LifecycleOwner,
    ViewModelStoreOwner,
    BackDispatcherOwner,
    androidx.lifecycle.LifecycleObserver {
    private val viewModel by viewModels<PreComposeViewModel>()
    override val lifecycle by lazy {
        LifecycleRegistry()
    }

    override val viewModelStore by lazy {
        viewModel.viewModelStore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            if (!backDispatcher.onBackPress()) {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycle.currentState = Lifecycle.State.Active
    }

    override fun onPause() {
        super.onPause()
        lifecycle.currentState = Lifecycle.State.InActive
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.currentState = Lifecycle.State.Destroyed
    }

    override val backDispatcher by lazy {
        BackDispatcher()
    }
}

fun PreComposeActivity.setContent(
    parent: CompositionContext? = null,
    content: @Composable () -> Unit
) {
    val existingComposeView = window.decorView
        .findViewById<ViewGroup>(android.R.id.content)
        .getChildAt(0) as? ComposeView

    if (existingComposeView != null) with(existingComposeView) {
        setParentCompositionContext(parent)
        setContent {
            ContentInternal(content)
        }
    } else ComposeView(this).apply {
        // Set content and parent **before** setContentView
        // to have ComposeView create the composition on attach
        setParentCompositionContext(parent)
        setContent {
            ContentInternal(content)
        }
        // Set the view tree owners before setting the content view so that the inflation process
        // and attach listeners will see them already present
        setOwners()
        setContentView(this, DefaultActivityContentLayoutParams)
    }
}

private fun PreComposeActivity.setOwners() {
    val decorView = window.decorView
    if (ViewTreeLifecycleOwner.get(decorView) == null) {
        ViewTreeLifecycleOwner.set(decorView, this)
    }
    if (decorView.findViewTreeSavedStateRegistryOwner() == null) {
        decorView.setViewTreeSavedStateRegistryOwner(this)
    }
}

@Composable
private fun PreComposeActivity.ContentInternal(content: @Composable () -> Unit) {
    ProvideAndroidCompositionLocals {
        content.invoke()
    }
}

@Composable
private fun PreComposeActivity.ProvideAndroidCompositionLocals(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLifecycleOwner provides this,
        LocalViewModelStoreOwner provides this,
        LocalBackDispatcherOwner provides this,
    ) {
        content.invoke()
    }
}

private val DefaultActivityContentLayoutParams = ViewGroup.LayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
)
