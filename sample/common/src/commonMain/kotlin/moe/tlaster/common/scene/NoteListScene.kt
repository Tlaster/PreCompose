package moe.tlaster.common.scene

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import moe.tlaster.common.model.Note

@ExperimentalMaterialApi
@Composable
fun NoteListScene(
    items: List<Note>,
    onItemClicked: (note: Note) -> Unit,
    onEditClicked: (note: Note) -> Unit,
    onDeleteClicked: (note: Note) -> Unit,
    onAddClicked: () -> Unit,
) {
    // val viewModel = viewModel {
    //     NoteListViewModel()
    // }
    // val items by viewModel.items.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Note")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddClicked.invoke() }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) {
        LazyColumn {
            items(items, key = { it.hashCode() }) {
                ListItem(
                    modifier = Modifier
                        .clickable {
                            onItemClicked.invoke(it)
                        },
                    text = {
                        Text(it.title)
                    },
                    trailing = {
                        Row {
                            TextButton(
                                onClick = {
                                    onEditClicked.invoke(it)
                                }
                            ) {
                                Text("Edit")
                            }
                            TextButton(
                                onClick = {
                                    // viewModel.delete(it)
                                    onDeleteClicked.invoke(it)
                                }
                            ) {
                                Text("Delete", color = Color.Red)
                            }
                        }
                    }
                )
            }
        }
    }
}
