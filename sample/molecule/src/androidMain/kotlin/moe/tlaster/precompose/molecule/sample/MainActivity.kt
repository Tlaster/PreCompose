package moe.tlaster.precompose.molecule.sample

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import moe.tlaster.precompose.lifecycle.setContent

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
