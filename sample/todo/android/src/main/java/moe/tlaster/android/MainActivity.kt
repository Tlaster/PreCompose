package moe.tlaster.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import moe.tlaster.common.App
import moe.tlaster.precompose.lifecycle.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
