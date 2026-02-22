package com.example.wepartyapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreenUI(onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile Coming Soon!",
            fontSize = 24.sp,
            color = Color.Black
        )

        Button(
            onClick = onEditClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Edit Profile Setup", color = Color.White)
        }
    }
}