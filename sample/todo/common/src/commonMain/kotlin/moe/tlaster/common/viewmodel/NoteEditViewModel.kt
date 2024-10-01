package moe.tlaster.common.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import moe.tlaster.common.repository.FakeRepository

class NoteEditViewModel(
    private val id: Int?,
    private val fakeRepository: FakeRepository,
) : ViewModel() {

    private val note by lazy {
        if (id != null) {
            fakeRepository.get(id)
        } else {
            null
        }
    }

    val title = MutableStateFlow(note?.title ?: "")
    val content = MutableStateFlow(note?.content ?: "")

    fun setTitle(value: String) {
        title.value = value
    }

    fun setContent(value: String) {
        content.value = value
    }

    fun save() {
        note?.let {
            fakeRepository.update(it.copy(title = title.value, content = content.value))
        } ?: run {
            fakeRepository.add(title = title.value, content = content.value)
        }
    }
}
