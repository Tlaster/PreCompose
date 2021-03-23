package moe.tlaster.common
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
fun App() {
    MaterialTheme {
        val navigator = rememberNavigator()
        NavHost(
            navigator = navigator,
            initialRoute = "/home"
        ) {
            scene("/home") {
                var text2 by rememberSaveable { mutableStateOf("") }
                Column {
                    Button(onClick = {
                        navigator.navigate("/detail/$text2?text=$text2")
                    }) {
                        Text("click me")
                    }
                    Button(onClick = {
                        navigator.navigate("/dialog")
                    }) {
                        Text("open dialog")
                    }
                    if (navigator.canGoBack) {
                        Button(onClick = {
                            navigator.goBack()
                        }) {
                            Text("go back !")
                        }
                    }
                    OutlinedTextField(
                        value = text2,
                        onValueChange = {
                            text2 = it
                        }
                    )
                }
            }
            scene("/detail/{id:[0-9]+}") {
                Column {
                    if (navigator.canGoBack) {
                        Button(onClick = {
                            navigator.goBack()
                        }) {
                            Text("go back !")
                        }
                    }
                    Button(onClick = {
                        navigator.navigate("/home")
                    }) {
                        Text("go home !")
                    }
                    Text("number")
                    it.path("id")?.let { it1 -> Text(it1) }
                    it.query("text")?.let { it1 -> Text(it1) }
                }
            }
            scene("/detail/{id:[a-z]+}") {
                Column {
                    if (navigator.canGoBack) {
                        Button(onClick = {
                            navigator.goBack()
                        }) {
                            Text("go back !")
                        }
                    }
                    Button(onClick = {
                        navigator.navigate("/home")
                    }) {
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
                            Button(onClick = {
                                navigator.goBack()
                            }) {
                                Text("go back !")
                            }
                        }
                        Button(onClick = {
                            navigator.navigate("/detail/21321?text=21321")
                        }) {
                            Text("click me")
                        }
                    }
                }
            }
        }
    }
}
