package moe.tlaster.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import moe.tlaster.common.scene.NoteDetailScene
import moe.tlaster.common.scene.NoteEditScene
import moe.tlaster.common.scene.NoteListScene
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.navigation.transition.fadeScaleCreateTransition
import moe.tlaster.precompose.navigation.transition.fadeScaleDestroyTransition

@ExperimentalMaterialApi
@Composable
fun App() {
    val navigator = rememberNavigator()
    BoxWithConstraints {
        MaterialTheme {
            NavHost(
                navigator = navigator,
                initialRoute = "/home"
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
                        }
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
                scene(
                    "/edit/{id:[0-9]+}?",
                    navTransition = NavTransition(
                        createTransition = {
                            translationY = constraints.maxHeight * (1 - it)
                        },
                        destroyTransition = {
                            translationY = constraints.maxHeight * (1 - it)
                        },
                        pauseTransition = fadeScaleDestroyTransition,
                        resumeTransition = fadeScaleCreateTransition,
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.path<Int>("id")
                    NoteEditScene(
                        id = id,
                        onDone = {
                            navigator.goBack()
                        },
                        onBack = {
                            navigator.goBack()
                        }
                    )
                }
            }
        }
    }
}
