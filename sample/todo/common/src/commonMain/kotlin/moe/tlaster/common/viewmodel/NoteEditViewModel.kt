package moe.tlaster.common.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.precompose.viewmodel.ViewModel

class NoteEditViewModel(
    private val id: Int?,
) : ViewModel() {

    private val note by lazy {
        if (id != null) {
            FakeRepository.get(id)
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
            FakeRepository.update(it.copy(title = title.value, content = content.value))
        } ?: run {
            FakeRepository.add(title = title.value, content = content.value)
        }
    }
}
