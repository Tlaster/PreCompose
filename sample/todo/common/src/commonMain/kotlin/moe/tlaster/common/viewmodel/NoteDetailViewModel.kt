package moe.tlaster.common.viewmodel

import androidx.lifecycle.ViewModel
import moe.tlaster.common.repository.FakeRepository

class NoteDetailViewModel(
    private val id: Int,
    private val fakeRepository: FakeRepository,
) : ViewModel() {
    val note by lazy {
        fakeRepository.getLiveData(id)
    }
}
