package moe.tlaster.precompose.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import moe.tlaster.precompose.PreComposeApp
import kotlin.test.Test

class NavHostTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navigationTest() = runComposeUiTest {
        setContent {
            PreComposeApp {
                val navigator = rememberNavigator()
                Column {
                    Button(onClick = {
                        navigator.goBack()
                    }, modifier = Modifier.testTag("goback")) {
                        Text("Go Back")
                    }
                    Button(onClick = {
                        navigator.navigate("/1")
                    }, modifier = Modifier.testTag("to1")) {
                        Text("1")
                    }
                    Button(onClick = {
                        navigator.navigate("/2")
                    }, modifier = Modifier.testTag("to2")) {
                        Text("2")
                    }
                    Button(onClick = {
                        navigator.navigate("/3")
                    }, modifier = Modifier.testTag("to3")) {
                        Text("3")
                    }
                    NavHost(
                        navigator = navigator,
                        initialRoute = "/1",
                    ) {
                        scene("/1") {
                            Text("1", modifier = Modifier.testTag("screen1"))
                            Text("1", modifier = Modifier.testTag("text"))
                        }
                        scene("/2") {
                            Text("2", modifier = Modifier.testTag("screen2"))
                            Text("2", modifier = Modifier.testTag("text"))
                        }
                        scene("/3") {
                            Text("3", modifier = Modifier.testTag("screen3"))
                            Text("3", modifier = Modifier.testTag("text"))
                        }
                    }
                }
            }
        }
        onNodeWithTag("text").assertTextEquals("1")
        onNodeWithTag("to2").performClick()
        onNodeWithTag("to3").performClick()
        onNodeWithTag("to1").performClick()
        onNodeWithTag("to3").performClick()
        onNodeWithTag("text").assertTextEquals("3")
    }
}
