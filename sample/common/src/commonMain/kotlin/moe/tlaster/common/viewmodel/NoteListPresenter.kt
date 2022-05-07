package moe.tlaster.common.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import moe.tlaster.common.model.Note
import moe.tlaster.common.repository.FakeRepository
import kotlin.time.Duration.Companion.milliseconds

class NoteListPresenter {
    @Composable
    fun present(intents: Flow<NoteListIntent>): NoteListState {
        var model by remember { mutableStateOf<NoteListState>(NoteListState.Loading) }
        LaunchedEffect(Unit) {
            delay(500.milliseconds)
            FakeRepository.items.collect { items ->
                model = NoteListState.Success(items)
            }
        }
        LaunchedEffect(Unit) {
            intents.collect {
                when (it) {
                    is NoteListIntent.Delete -> {
                        FakeRepository.remove(it.note)
                    }
                }
            }
        }
        return model
    }
}

sealed class NoteListIntent {
    data class Delete(val note: Note) : NoteListIntent()
}

sealed class NoteListState {
    object Loading : NoteListState()

    data class Success(val list: List<Note>) : NoteListState()
}