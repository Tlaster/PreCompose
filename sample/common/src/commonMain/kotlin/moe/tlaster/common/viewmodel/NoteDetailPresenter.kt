package moe.tlaster.common.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import moe.tlaster.common.model.Note
import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.common.util.rememberInMemory

class NoteDetailPresenter(
    private val id: Int,
) {
    @Composable
    fun present(intents: Flow<NoteDetailIntent>): NoteDetailState {
        val noteFlow = rememberInMemory { FakeRepository.getStateFlow(id) }
        val note by noteFlow.collectAsState(null)
        return makeState(note)
    }
}

private fun makeState(note: Note?) = if (note != null) {
    NoteDetailState.Success(note)
} else {
    NoteDetailState.Loading
}

sealed class NoteDetailIntent

sealed class NoteDetailState {
    object Loading : NoteDetailState()
    data class Success(val note: Note) : NoteDetailState()
}
