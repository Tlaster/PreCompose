package moe.tlaster.common.viewmodel

import moe.tlaster.common.model.Note
import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.precompose.viewmodel.ViewModel

class NoteListViewModel : ViewModel() {
    val items by lazy {
        FakeRepository.items
    }
    fun delete(note: Note) {
        FakeRepository.remove(note = note)
    }
}
