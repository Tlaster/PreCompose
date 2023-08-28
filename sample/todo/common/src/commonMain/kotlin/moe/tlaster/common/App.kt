package moe.tlaster.common

import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import moe.tlaster.common.repository.FakeRepository
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
import moe.tlaster.precompose.stateholder.SavedStateHolder
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun setupKoin() {
    startKoin {
        modules(
            module {
                single { FakeRepository() }
                factory { (id: Int) ->
                    NoteDetailViewModel(
                        id = id,
                        fakeRepository = get(),
                    )
                }
                factory { (id: Int?, savedStateHolder: SavedStateHolder) ->
                    NoteEditViewModel(
                        id = id,
                        savedStateHolder = savedStateHolder,
                        fakeRepository = get(),
                    )
                }
                factory { NoteListViewModel(fakeRepository = get()) }
            },
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App() {
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
            scene(
                "/edit/{id:[0-9]+}?",
                navTransition = NavTransition(
                    createTransition = slideInVertically(initialOffsetY = { it }),
                    destroyTransition = slideOutVertically(targetOffsetY = { it }),
                    pauseTransition = scaleOut(targetScale = 0.9f),
                    resumeTransition = scaleIn(initialScale = 0.9f),
                    exitTargetContentZIndex = 1f,
                ),
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
