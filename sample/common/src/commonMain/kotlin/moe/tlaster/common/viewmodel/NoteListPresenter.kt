package moe.tlaster.common.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import moe.tlaster.common.model.Note
import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.common.util.rememberInMemory

class NoteListPresenter {
    @Composable
    fun present(intents: Flow<NoteListIntent>): NoteListState {
        val listFlow = rememberInMemory { FakeRepository.items }
        val list by listFlow.collectAsState(null)
        LaunchedEffect(Unit) {
            intents.collect {
                when (it) {
                    is NoteListIntent.Delete -> {
                        FakeRepository.remove(it.note)
                    }
                }
            }
        }
        return makeState(list)
    }
}

private fun makeState(list: List<Note>?) = if (list != null) {
    NoteListState.Success(list)
} else {
    NoteListState.Loading
}

sealed class NoteListIntent {
    data class Delete(val note: Note) : NoteListIntent()
}

sealed class NoteListState {
    object Loading : NoteListState()
    data class Success(val list: List<Note>) : NoteListState()
}
