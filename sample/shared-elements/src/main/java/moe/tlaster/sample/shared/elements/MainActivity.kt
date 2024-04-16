@file:OptIn(ExperimentalSharedTransitionApi::class)

package moe.tlaster.sample.shared.elements

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.contentSize
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PreComposeApp {
                App()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun App() {
    SharedTransitionLayout(Modifier.fillMaxSize()) {
        val navigator = rememberNavigator()
        NavHost(
            navigator,
            "a",
            Modifier.fillMaxSize()
        ) {
            scene("a") {
                with(SharedElementsScope(this@SharedTransitionLayout, this)) {
                    Box(Modifier.fillMaxSize()) {
                        A(navigateToB = { index -> navigator.navigate("b/$index") })
                    }
                }
            }
            scene(
                "b/{id}",
            ) { backStackEntry ->
                val index: Int = backStackEntry.path<Int>("id")!!
                with(SharedElementsScope(this@SharedTransitionLayout, this)) {
                    Box(Modifier.fillMaxSize()) {
                        B(colors[index])
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedElementsScope.A(
    navigateToB: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for ((index, color) in colors.withIndex()) {
            Card(
                color,
                Modifier
                    .padding(16.dp)
                    .background(color)
                    .clickable { navigateToB(index) }
                    .sharedElement(this@A, rememberSharedContentState(color.toString()))
            ) {
                BasicText("Color:#${color.value}")
            }
        }
    }
}

@Composable
private fun SharedElementsScope.B(color: Color, modifier: Modifier = Modifier) {
    Card(
        color,
        modifier
            .fillMaxSize()
            .wrapContentSize()
            .sharedElement(this, rememberSharedContentState(color.toString()))
    ) {
        BasicText("Color:#${color.value}")
    }
}

@Composable
private fun Card(color: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth().height(50.dp).background(color)) {
        content()
    }
}

private val colors = listOf(
    Color.Gray,
    Color.LightGray,
    Color.White,
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Yellow,
    Color.Cyan,
    Color.Magenta,
)

private class SharedElementsScope(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedContentScope: AnimatedContentScope,
)

@Composable
private fun SharedElementsScope.rememberSharedContentState(key: Any): SharedTransitionScope.SharedContentState {
    return with(sharedTransitionScope) {
        rememberSharedContentState(key)
    }
}

private fun Modifier.sharedElement(
    sharedElementsScope: SharedElementsScope,
    state: SharedTransitionScope.SharedContentState,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeHolderSize: SharedTransitionScope.PlaceHolderSize = contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: SharedTransitionScope.OverlayClip = ParentClip
) = with(sharedElementsScope) {
    with(sharedTransitionScope) {
        this@sharedElement.sharedElement(
            state = state,
            animatedVisibilityScope = animatedContentScope,
            boundsTransform = boundsTransform,
            placeHolderSize = placeHolderSize,
            renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            zIndexInOverlay = zIndexInOverlay,
            clipInOverlayDuringTransition = clipInOverlayDuringTransition,
        )
    }
}

private val ParentClip: SharedTransitionScope.OverlayClip =
    object : SharedTransitionScope.OverlayClip {
        override fun getClipPath(
            state: SharedTransitionScope.SharedContentState,
            bounds: Rect,
            layoutDirection: LayoutDirection,
            density: Density
        ): Path? {
            return state.parentSharedContentState?.clipPathInOverlay
        }
    }
private val DefaultBoundsTransform = BoundsTransform { _, _ -> DefaultSpring }
private val DefaultSpring = spring(
    stiffness = StiffnessMediumLow,
    visibilityThreshold = Rect.VisibilityThreshold
)