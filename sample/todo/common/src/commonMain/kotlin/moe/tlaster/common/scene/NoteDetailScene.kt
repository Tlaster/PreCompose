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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import moe.tlaster.common.viewmodel.NoteDetailViewModel
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.core.parameter.parametersOf

@ExperimentalMaterialApi
@Composable
fun NoteDetailScene(
    id: Int,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val viewModel = koinViewModel(NoteDetailViewModel::class) { parametersOf(id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detail")
                },
                navigationIcon = {
                    IconButton(onClick = { onBack.invoke() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit.invoke() }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                },
            )
        },
    ) {
        Column {
            val note by viewModel.note.collectAsState()
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
