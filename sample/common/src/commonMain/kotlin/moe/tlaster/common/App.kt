package moe.tlaster.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableSharedFlow
import moe.tlaster.common.scene.NoteDetailScene
import moe.tlaster.common.scene.NoteEditScene
import moe.tlaster.common.scene.NoteListScene
import moe.tlaster.common.viewmodel.NoteDetailIntent
import moe.tlaster.common.viewmodel.NoteDetailPresenter
import moe.tlaster.common.viewmodel.NoteDetailState
import moe.tlaster.common.viewmodel.NoteEditIntent
import moe.tlaster.common.viewmodel.NoteEditPresenter
import moe.tlaster.common.viewmodel.NoteListIntent
import moe.tlaster.common.viewmodel.NoteListPresenter
import moe.tlaster.common.viewmodel.NoteListState
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
                    val noteListPresenter = remember { NoteListPresenter() }
                    val intentsFlow =
                        remember { MutableSharedFlow<NoteListIntent>(extraBufferCapacity = 1) }

                    when (val model = noteListPresenter.present(intentsFlow)) {
                        NoteListState.Loading -> {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is NoteListState.Success -> {
                            NoteListScene(
                                items = model.list,
                                onItemClicked = {
                                    navigator.navigate("/detail/${it.id}")
                                },
                                onDeleteClicked = {
                                    intentsFlow.tryEmit(
                                        NoteListIntent.Delete(it)
                                    )
                                },
                                onEditClicked = {
                                    navigator.navigate("/edit/${it.id}")
                                },
                                onAddClicked = {
                                    navigator.navigate("/edit")
                                },
                            )
                        }
                    }
                }
                scene("/detail/{id:[0-9]+}") { backStackEntry ->
                    val id = backStackEntry.path<Int>("id")!!
                    val noteDetailPresenter = remember(id) { NoteDetailPresenter(id) }
                    val intentsFlow =
                        remember { MutableSharedFlow<NoteDetailIntent>(extraBufferCapacity = 1) }

                    when (val model = noteDetailPresenter.present(intentsFlow)) {
                        NoteDetailState.Loading -> {
                        }
                        is NoteDetailState.Success -> {
                            NoteDetailScene(
                                note = model.note,
                                onEdit = {
                                    navigator.navigate("/edit/$id")
                                },
                                onBack = {
                                    navigator.goBack()
                                },
                            )
                        }
                    }
                }
                scene(
                    "/edit/{id:[0-9]+}?",
                    navTransition = NavTransition(
                        createTransition = {
                            translationY = constraints.maxHeight * (1 - it)
                            alpha = it
                        },
                        destroyTransition = {
                            translationY = constraints.maxHeight * (1 - it)
                            alpha = it
                        },
                        pauseTransition = fadeScaleDestroyTransition,
                        resumeTransition = fadeScaleCreateTransition,
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.path<Int>("id")
                    val noteEditPresenter = remember(id) { NoteEditPresenter(id) }
                    val intentsFlow =
                        remember { MutableSharedFlow<NoteEditIntent>(extraBufferCapacity = 1) }

                    val model = noteEditPresenter.present(intentsFlow)
                    NoteEditScene(
                        isCreate = id == null,
                        title = model.title,
                        onTitleChanged = {
                            intentsFlow.tryEmit(
                                NoteEditIntent.TitleChanged(it)
                            )
                        },
                        content = model.content,
                        onContentChanged = {
                            intentsFlow.tryEmit(
                                NoteEditIntent.ContentChanged(it)
                            )
                        },
                        onDone = {
                            intentsFlow.tryEmit(
                                NoteEditIntent.Save
                            )
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
