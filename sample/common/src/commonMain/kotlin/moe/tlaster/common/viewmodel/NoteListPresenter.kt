package moe.tlaster.common.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import moe.tlaster.common.model.Note
import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.common.util.rememberInMemory
import kotlin.time.Duration.Companion.milliseconds

class NoteListPresenter {
    @Composable
    fun present(intents: Flow<NoteListIntent>): NoteListState {
        var model by rememberInMemory {
            mutableStateOf<List<Note>?>(null)
        }
        LaunchedEffect(Unit) {
            delay(500.milliseconds)
            FakeRepository.items.collect { items ->
                model = items
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
        return if (model != null) {
            NoteListState.Success(model!!)
        } else {
            NoteListState.Loading
        }
    }
}

sealed class NoteListIntent {
    data class Delete(val note: Note) : NoteListIntent()
}

sealed class NoteListState {
    object Loading : NoteListState()

    data class Success(val list: List<Note>) : NoteListState()
}
