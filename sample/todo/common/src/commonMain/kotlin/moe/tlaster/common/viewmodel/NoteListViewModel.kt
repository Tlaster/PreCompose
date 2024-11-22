package moe.tlaster.common.viewmodel

import androidx.lifecycle.ViewModel
import moe.tlaster.common.model.Note
import moe.tlaster.common.repository.FakeRepository

class NoteListViewModel(
    private val fakeRepository: FakeRepository,
) : ViewModel() {
    val items by lazy {
        fakeRepository.items
    }
    fun delete(note: Note) {
        fakeRepository.remove(note = note)
    }
}
