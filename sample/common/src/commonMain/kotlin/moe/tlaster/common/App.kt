package moe.tlaster.common
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
        }
    }
}
