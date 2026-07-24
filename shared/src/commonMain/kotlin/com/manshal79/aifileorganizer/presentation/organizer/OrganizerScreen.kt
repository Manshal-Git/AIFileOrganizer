package com.manshal79.aifileorganizer.presentation.organizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manshal79.aifileorganizer.domain.model.FileType
import com.manshal79.aifileorganizer.presentation.asString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OrganizerRoot(
    viewModel: OrganizerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OrganizerScreen(
        state = state,
        onAction = viewModel::onAction,
        onPickFolderClick = {
            pickDirectory()?.let { path ->
                viewModel.onAction(OrganizerAction.OnFolderSelected(path))
            }
        },
    )
}

@Composable
fun OrganizerScreen(
    state: OrganizerState,
    onAction: (OrganizerAction) -> Unit,
    onPickFolderClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onPickFolderClick) {
                Text("Choose Folder")
            }
            Spacer(Modifier.width(12.dp))
            Text(state.folderPath ?: "No folder selected")
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Auto-pilot")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = state.mode == OrganizeMode.AUTO_PILOT,
                onCheckedChange = { onAction(OrganizerAction.OnToggleMode) },
            )
        }

        state.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.isScanning -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            state.files.isEmpty() -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text("No files scanned yet.")
            }

            else -> LazyColumn {
                items(state.files, key = { it.id }) { file ->
                    FileRow(file)
                }
            }
        }
    }
}

@Composable
private fun FileRow(file: FileItemUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(file.name, modifier = Modifier.weight(1f))
        Text(file.type.name, style = MaterialTheme.typography.labelSmall)
    }
}

@Preview
@Composable
private fun OrganizerScreenPreview() {
    MaterialTheme {
        OrganizerScreen(
            state = OrganizerState(
                folderPath = "/Users/manshal/Documents/Inbox",
                files = listOf(
                    FileItemUi(id = "1", name = "invoice_scan.pdf", path = "/1", type = FileType.PDF),
                    FileItemUi(id = "2", name = "vacation.jpg", path = "/2", type = FileType.IMAGE),
                    FileItemUi(id = "3", name = "notes.txt", path = "/3", type = FileType.TEXT_DOCUMENT),
                ),
            ),
            onAction = {},
            onPickFolderClick = {},
        )
    }
}
