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
import kotlinx.coroutines.flow.Flow
import moe.tlaster.precompose.molecule.collectAction
import moe.tlaster.precompose.molecule.rememberPresenter

@Composable
fun App() {
    val (state, channel) = rememberPresenter { Presenter(it) }
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
                        channel.trySend(Action.Increment)
                    }
                ) {
                    Text(text = "Increment")
                }
                Button(
                    onClick = {
                        channel.trySend(Action.Decrement)
                    }
                ) {
                    Text(text = "Decrement")
                }
            }
        }
    }
}

@Composable
fun Presenter(
    action: Flow<Action>,
): State {
    var count by remember { mutableStateOf(0) }

    action.collectAction {
        when (this) {
            Action.Increment -> count++
            Action.Decrement -> count--
        }
    }

    return State("Clicked $count times")
}

sealed interface Action {
    object Increment : Action
    object Decrement : Action
}

data class State(
    val count: String,
)
