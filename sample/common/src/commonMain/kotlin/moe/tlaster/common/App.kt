package moe.tlaster.common
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
fun App() {
    MaterialTheme {
        val navigator = rememberNavigator()
        NavHost(
            navigator = navigator,
            initialRoute = "home"
        ) {
            scene("home") {
                Column {
                    Button(onClick = {
                        navigator.navigate("detail")
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
                    var text2 by rememberSaveable { mutableStateOf("") }
                    OutlinedTextField(
                        value = text2,
                        onValueChange = {
                            text2 = it
                        }
                    )
                }
            }
            scene("detail") {
                Column {
                    if (navigator.canGoBack) {
                        Button(onClick = {
                            navigator.goBack()
                        }) {
                            Text("go back !")
                        }
                    }
                    Button(onClick = {
                        navigator.navigate("home")
                    }) {
                        Text("go home !")
                    }
                }
            }
        }
    }
}
