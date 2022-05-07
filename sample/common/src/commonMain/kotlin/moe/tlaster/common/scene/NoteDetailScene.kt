package moe.tlaster.common.scene

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import moe.tlaster.common.model.Note

@ExperimentalMaterialApi
@Composable
fun NoteDetailScene(
    // id: Int,
    note: Note,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    // val viewModel = viewModel(listOf(id)) {
    //     NoteDetailViewModel(id)
    // }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detail")
                },
                navigationIcon = {
                    IconButton(onClick = { onBack.invoke() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit.invoke() }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                }
            )
        }
    ) {
        Column {
            // val note by viewModel.note.observeAsState()
            ListItem {
                Text(text = note.title, style = MaterialTheme.typography.h5)
            }
            Divider()
            ListItem {
                Text(text = note.content)
            }
        }
    }
}
