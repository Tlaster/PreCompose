package moe.tlaster.common.scene

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import moe.tlaster.common.viewmodel.NoteEditViewModel
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import moe.tlaster.precompose.stateholder.LocalSavedStateHolder
import org.koin.core.parameter.parametersOf

@ExperimentalMaterialApi
@Composable
fun NoteEditScene(
    id: Int? = null,
    onDone: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val stateHolder = LocalSavedStateHolder.current
    val viewModel = koinViewModel<NoteEditViewModel> { parametersOf(id, stateHolder) }

    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.save()
                            onDone.invoke()
                        },
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                    }
                },
                title = {
                    if (id == null) {
                        Text("Create")
                    } else {
                        Text("Edit")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack.invoke() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) {
        Column {
            val title by viewModel.title.collectAsStateWithLifecycle()
            val content by viewModel.content.collectAsStateWithLifecycle()
            ListItem {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = title,
                    onValueChange = {
                        viewModel.setTitle(it)
                    },
                    placeholder = {
                        Text("Title")
                    },
                )
            }
            ListItem(
                modifier = Modifier
                    .weight(1f),
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize(),
                    value = content,
                    onValueChange = {
                        viewModel.setContent(it)
                    },
                    placeholder = {
                        Text("Content")
                    },
                )
            }
        }
    }
}
