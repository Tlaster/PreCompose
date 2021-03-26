package moe.tlaster.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import moe.tlaster.precompose.livedata.LiveData
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.ui.observeAsState
import moe.tlaster.precompose.ui.viewModel
import moe.tlaster.precompose.viewmodel.ViewModel

@Composable
fun App() {
    MaterialTheme {
        val navigator = rememberNavigator()
        val outerKeys = listOf("home")
        NavHost(
            navigator = navigator,
            initialRoute = "/home"
        ) {
            scene("/home") {
                val viewModel = viewModel(outerKeys) {
                    HomeViewModel()
                }
                val text2 by viewModel.text.observeAsState()
                Column {
                    Button(
                        onClick = {
                            navigator.navigate("/detail/$text2?text=$text2")
                        }
                    ) {
                        Text("click me")
                    }
                    Button(
                        onClick = {
                            navigator.navigate("/dialog")
                        }
                    ) {
                        Text("open dialog")
                    }
                    if (navigator.canGoBack) {
                        Button(
                            onClick = {
                                navigator.goBack()
                            }
                        ) {
                            Text("go back !")
                        }
                    }
                    OutlinedTextField(
                        value = text2,
                        onValueChange = {
                            viewModel.setText(it)
                        }
                    )
                    Text(text2)
                }
            }
            scene("/detail/{id:[0-9]+}") {
                Column {
                    if (navigator.canGoBack) {
                        Button(
                            onClick = {
                                navigator.goBack()
                            }
                        ) {
                            Text("go back !")
                        }
                    }
                    Button(
                        onClick = {
                            navigator.navigate("/home")
                        }
                    ) {
                        Text("go home !")
                    }
                    Text("number")
                    it.path("id")?.let { it1 -> Text(it1) }
                    it.query("text")?.let { it1 -> Text(it1) }
                }
            }
            scene(
                "/detail/{id:[a-z]+}",
                navTransition = NavTransition(
                    createTransition = {
                        translationY = (1 - it) * 200f
                        alpha = it
                    },
                    destroyTransition = {
                        translationY = (1 - it) * 200f
                        alpha = it
                    },
                    resumeTransition = {},
                    pauseTransition = {}
                )
            ) {
                Column {
                    if (navigator.canGoBack) {
                        Button(
                            onClick = {
                                navigator.goBack()
                            }
                        ) {
                            Text("go back !")
                        }
                    }
                    Button(
                        onClick = {
                            navigator.navigate("/home")
                        }
                    ) {
                        Text("go home !")
                    }
                    Text("string")
                    it.path("id")?.let { it1 -> Text(it1) }
                    it.query("text")?.let { it1 -> Text(it1) }
                }
            }
            dialog("/dialog") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.54f))
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colors.background)
                    ) {
                        Text("this is dialog")
                        if (navigator.canGoBack) {
                            Button(
                                onClick = {
                                    navigator.goBack()
                                }
                            ) {
                                Text("go back !")
                            }
                        }
                        Button(
                            onClick = {
                                navigator.navigate("/detail/21321?text=21321")
                            }
                        ) {
                            Text("click me")
                        }
                    }
                }
            }
        }
    }
}

class HomeViewModel : ViewModel() {
    val text = LiveData("")
    fun setText(value: String) {
        text.value = value
    }
}
