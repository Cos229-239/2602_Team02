package com.example.wepartyapp.ui.create_event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

// UI for the Add Items screen
@Composable
fun AddItemsScreenUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Add Items (Coming Soon)",
            color = Color.Black,
            fontSize = 18.sp
        )
    }
}