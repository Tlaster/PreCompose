package moe.tlaster.precompose.molecule.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.molecule.producePresenter

@Composable
fun App() {
    val state by producePresenter { Presenter() }
    MaterialTheme {
        Scaffold {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = state.count)
                Button(
                    onClick = {
                        state.action(Action.Increment)
                    }
                ) {
                    Text(text = "Increment")
                }
                Button(
                    onClick = {
                        state.action(Action.Decrement)
                    }
                ) {
                    Text(text = "Decrement")
                }
            }
        }
    }
}

@Composable
fun Presenter(): State {
    var count by remember { mutableStateOf(0) }
    return State(
        "Clicked $count times",
    ) {
        when (it) {
            Action.Increment -> count++
            Action.Decrement -> count--
        }
    }
}

sealed interface Action {
    object Increment : Action
    object Decrement : Action
}

data class State(
    val count: String,
    val action: (Action) -> Unit,
)
