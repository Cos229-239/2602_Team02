package com.example.wepartyapp.ui.create_event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Event Details Screen
@Composable
fun EventDetailsScreenUI() {
    Box(                                                         //outermost layer
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(onClick = {})                                        //back to home pg btn
            {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Home"
                )
            }
            Icon(                                                           //add items icon
                imageVector = Icons.Default.Create,
                contentDescription = null
            )
            Text(                                                           //pg title
                text = "Event Details (Coming Soon)",
                color = Color.Black,
                fontSize = 18.sp
            )
        }
    }
}