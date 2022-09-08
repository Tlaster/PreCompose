package moe.tlaster.common.viewmodel

import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.precompose.viewmodel.ViewModel

class NoteDetailViewModel(
    private val id: Int,
) : ViewModel() {
    val note by lazy {
        FakeRepository.getLiveData(id)
    }
}
