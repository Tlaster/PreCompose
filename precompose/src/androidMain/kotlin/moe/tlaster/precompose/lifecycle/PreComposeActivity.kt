package moe.tlaster.precompose.lifecycle

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import moe.tlaster.precompose.PreComposeApp

fun ComponentActivity.setContent(
    parent: CompositionContext? = null,
    content: @Composable () -> Unit,
) {
    val existingComposeView =
        window.decorView.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as? ComposeView

    if (existingComposeView != null) {
        with(existingComposeView) {
            setParentCompositionContext(parent)
            setContent {
                ContentInternal(content)
            }
        }
    } else {
        ComposeView(this).apply {
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
}

private fun ComponentActivity.setOwners() {
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
private fun ComponentActivity.ContentInternal(content: @Composable () -> Unit) {
    PreComposeApp {
        content.invoke()
    }
}

private val DefaultActivityContentLayoutParams = ViewGroup.LayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT,
)
