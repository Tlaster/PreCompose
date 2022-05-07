package moe.tlaster.common.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import moe.tlaster.common.model.Note
import moe.tlaster.common.repository.FakeRepository

class NoteDetailPresenter(
    private val id: Int,
)  {
    @Composable
    fun present(intents: Flow<NoteDetailIntent>): NoteDetailState {
        var model by remember { mutableStateOf<NoteDetailState>(NoteDetailState.Loading) }
        LaunchedEffect(Unit) {
            FakeRepository.getStateFlow(id).collect {
                model = NoteDetailState.Success(it)
            }
        }
        return model
    }
}

sealed class NoteDetailIntent

sealed class NoteDetailState {
    object Loading : NoteDetailState()
    data class Success(val note: Note) : NoteDetailState()
}
