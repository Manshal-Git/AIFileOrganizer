package com.manshal79.aifileorganizer

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.manshal79.aifileorganizer.presentation.designsystem.AppTheme
import com.manshal79.aifileorganizer.presentation.organizer.OrganizerRoot

@Composable
@Preview
fun App() {
    AppTheme {
        OrganizerRoot()
    }
}
