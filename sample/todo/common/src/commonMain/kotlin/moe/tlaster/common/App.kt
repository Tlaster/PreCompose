package moe.tlaster.common

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import moe.tlaster.common.scene.NoteDetailScene
import moe.tlaster.common.scene.NoteEditScene
import moe.tlaster.common.scene.NoteListScene
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator
import org.koin.compose.KoinContext

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App() {
    PreComposeApp {
        KoinContext {
            val navigator = rememberNavigator()
            MaterialTheme {
                NavHost(
                    navigator = navigator,
                    initialRoute = "/home",
                ) {
                    scene("/home") {
                        NoteListScene(
                            onItemClicked = {
                                navigator.navigate("/detail/${it.id}")
                            },
                            onAddClicked = {
                                navigator.navigate("/edit")
                            },
                            onEditClicked = {
                                navigator.navigate("/edit/${it.id}")
                            },
                        )
                    }
                    scene("/detail/{id:[0-9]+}") { backStackEntry ->
                        backStackEntry.path<Int>("id")?.let {
                            NoteDetailScene(
                                id = it,
                                onEdit = {
                                    navigator.navigate("/edit/$it")
                                },
                                onBack = {
                                    navigator.goBack()
                                },
                            )
                        }
                    }
                    dialog(
                        "/edit/{id:[0-9]+}?",
                    ) { backStackEntry ->
                        val id = backStackEntry.path<Int>("id")
                        NoteEditScene(
                            id = id,
                            onDone = {
                                navigator.goBack()
                            },
                            onBack = {
                                navigator.goBack()
                            },
                        )
                    }
                }
            }
        }
    }
}
