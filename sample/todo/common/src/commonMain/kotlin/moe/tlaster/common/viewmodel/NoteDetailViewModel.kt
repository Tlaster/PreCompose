package moe.tlaster.common.viewmodel

import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.precompose.viewmodel.ViewModel

class NoteDetailViewModel(
    private val id: Int,
    private val fakeRepository: FakeRepository,
) : ViewModel() {
    val note by lazy {
        fakeRepository.getLiveData(id)
    }
}
