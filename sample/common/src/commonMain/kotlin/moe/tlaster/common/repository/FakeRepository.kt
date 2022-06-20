package moe.tlaster.common.repository

import kotlinx.coroutines.flow.MutableStateFlow
import moe.tlaster.common.model.Note

private val fakeInitialNotes = mutableListOf(
    Note(0, title = "This is my first note", content = "Wow!"),
    Note(1, title = "You can create one", content = "Sounds great!"),
    Note(2, title = "Or you can edit one", content = "Awesome!"),
    Note(3, title = "You can also delete one", content = "Ok!"),
    Note(4, title = "Finally, you can click one to view", content = "Thanks!"),
)

object FakeRepository {
    private val watchers = hashMapOf<Int, MutableStateFlow<Note>>()
    val items = MutableStateFlow(fakeInitialNotes)

    fun get(id: Int): Note? {
        return items.value.firstOrNull { it.id == id }
    }

    fun add(title: String, content: String) {
        items.value.let {
            items.value = (it + Note(id = items.value.size, title = title, content = content)).toMutableList()
        }
    }

    fun remove(note: Note) {
        items.value.let {
            items.value = (it - note).toMutableList()
        }
    }

    fun update(note: Note) {
        watchers[note.id]?.let {
            it.value = note
        }
        get(note.id)?.let { n ->
            items.value.let { list ->
                list[list.indexOf(n)] = note
                items.value = list
            }
        }
    }

    fun getLiveData(id: Int): MutableStateFlow<Note> {
        return get(id)?.let {
            watchers.getOrPut(id) {
                MutableStateFlow(it)
            }
        } ?: throw IllegalArgumentException()
    }
}
