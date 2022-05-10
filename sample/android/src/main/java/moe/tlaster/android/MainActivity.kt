package moe.tlaster.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import moe.tlaster.common.App
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

class MainActivity : AppCompatActivity(), BackDispatcherOwner {

    override val backDispatcher: BackDispatcher by lazy(LazyThreadSafetyMode.NONE) {
        BackDispatcher()
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(
                LocalBackDispatcherOwner provides this,
            ) {
                App()
            }
        }
    }

    override fun onBackPressed() {
        if (!backDispatcher.onBackPress()) {
            super.onBackPressed()
        }
    }
}
