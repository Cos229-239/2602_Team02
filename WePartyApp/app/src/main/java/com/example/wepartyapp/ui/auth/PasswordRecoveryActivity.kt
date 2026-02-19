package com.example.wepartyapp.ui.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

class PasswordRecoveryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PasswordRecoveryScreenUI()
        }
    }
}

// UI for Password Recovery Screen
@Composable
fun PasswordRecoveryScreenUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Password Recovery (Coming Soon)",
            color = Color.Black,
            fontSize = 18.sp
        )
    }
}