package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberNavigator(): Navigator {
    return remember { Navigator() }
}

class Navigator {
    internal lateinit var stackManager: SceneStackManager

    fun navigate(route: String) {
        stackManager.navigate(route)
    }

    fun goBack() {
        stackManager.goBack()
    }

    val canGoBack: Boolean
        get() = stackManager.canGoBack
}