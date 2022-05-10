package moe.tlaster.common.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import moe.tlaster.common.model.Note
import moe.tlaster.common.repository.FakeRepository

class NoteEditPresenter(
    private val id: Int?,
) {
    @Composable
    fun present(intents: Flow<NoteEditIntent>): NoteEditState {
        var title by rememberSaveable { mutableStateOf("") }
        var content by rememberSaveable { mutableStateOf("") }

        LaunchedEffect(Unit) {
            if (id != null) {
                val node = FakeRepository.get(id)
                if (node != null) {
                    title = node.title
                    content = node.content
                }
            }
        }

        LaunchedEffect(Unit) {
            intents.collect { intent ->
                when (intent) {
                    is NoteEditIntent.TitleChanged -> {
                        title = intent.value
                    }
                    is NoteEditIntent.ContentChanged -> {
                        content = intent.value
                    }
                    NoteEditIntent.Save -> {
                        if (id != null) {
                            FakeRepository.update(
                                Note(
                                    id = id,
                                    title = title,
                                    content = content,
                                )
                            )
                        } else {
                            FakeRepository.add(
                                title = title,
                                content = content,
                            )
                        }
                    }
                }
            }
        }

        return NoteEditState(
            title = title,
            content = content,
        )
    }
}

sealed class NoteEditIntent {
    data class TitleChanged(val value: String) : NoteEditIntent()
    data class ContentChanged(val value: String) : NoteEditIntent()
    object Save : NoteEditIntent()
}

data class NoteEditState(
    val title: String,
    val content: String,
)
