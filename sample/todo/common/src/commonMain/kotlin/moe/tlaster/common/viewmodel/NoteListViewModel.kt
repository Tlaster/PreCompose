package moe.tlaster.common.viewmodel

import moe.tlaster.common.model.Note
import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.precompose.viewmodel.ViewModel

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
