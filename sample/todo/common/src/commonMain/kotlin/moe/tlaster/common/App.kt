package moe.tlaster.common

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import moe.tlaster.common.scene.NoteDetailScene
import moe.tlaster.common.scene.NoteEditScene
import moe.tlaster.common.scene.NoteListScene
import moe.tlaster.common.viewmodel.NoteDetailViewModel
import moe.tlaster.common.viewmodel.NoteEditViewModel
import moe.tlaster.common.viewmodel.NoteListViewModel
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.viewmodel.viewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun App() {
    val navigator = rememberNavigator()
    MaterialTheme {
        NavHost(
            navigator = navigator,
            initialRoute = "/home"
        ) {
            scene("/home") {
                val viewModel = viewModel(NoteListViewModel::class) {
                    println("create NoteListViewModel")
                    NoteListViewModel()
                }
                NoteListScene(
                    viewModel = viewModel,
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
                backStackEntry.path<Int>("id")?.let { id ->
                    val viewModel = viewModel(NoteDetailViewModel::class, listOf(id)) {
                        println("create NoteDetailViewModel")
                        NoteDetailViewModel(id)
                    }
                    NoteDetailScene(
                        viewModel = viewModel,
                        onEdit = {
                            navigator.navigate("/edit/$id")
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
                    createTransition = slideInVertically(initialOffsetY = { it }),
                    destroyTransition = slideOutVertically(targetOffsetY = { it }),
                    pauseTransition = scaleOut(targetScale = 0.9f),
                    resumeTransition = scaleIn(initialScale = 0.9f),
                    exitTargetContentZIndex = 1f,
                )
            ) { backStackEntry ->
                val id = backStackEntry.path<Int>("id")
                val viewModel = viewModel(NoteEditViewModel::class, listOf(id)) {
                    println("create NoteEditViewModel")
                    NoteEditViewModel(id)
                }
                NoteEditScene(
                    viewModel = viewModel,
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
