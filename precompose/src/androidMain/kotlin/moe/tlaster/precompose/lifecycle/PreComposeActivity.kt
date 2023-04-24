package moe.tlaster.precompose.lifecycle

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

open class PreComposeActivity : ComponentActivity() {
    internal val viewModel by viewModels<PreComposeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, viewModel.backPressedCallback)
    }

    override fun onResume() {
        super.onResume()
        viewModel.lifecycleRegistry.currentState = Lifecycle.State.Active
    }

    override fun onPause() {
        super.onPause()
        viewModel.lifecycleRegistry.currentState = Lifecycle.State.InActive
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            viewModel.lifecycleRegistry.currentState = Lifecycle.State.Destroyed
        }
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
    if (decorView.findViewTreeLifecycleOwner() == null) {
        decorView.setViewTreeLifecycleOwner(this)
    }
    if (decorView.findViewTreeViewModelStoreOwner() == null) {
        decorView.setViewTreeViewModelStoreOwner(this)
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
    val state by viewModel.backDispatcher.canHandleBackPress.collectAsState(false)
    LaunchedEffect(state) {
        viewModel.backPressedCallback.isEnabled = state
    }
    CompositionLocalProvider(
        LocalLifecycleOwner provides this.viewModel,
        LocalStateHolder provides this.viewModel.stateHolder,
        LocalBackDispatcherOwner provides this.viewModel,
    ) {
        content.invoke()
    }
}

private val DefaultActivityContentLayoutParams = ViewGroup.LayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
)
