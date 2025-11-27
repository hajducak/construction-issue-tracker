package com.hajducakmarek.fixit.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun IssueImage(
    photoPath: String,
    contentDescription: String,
    modifier: Modifier = Modifier
)
