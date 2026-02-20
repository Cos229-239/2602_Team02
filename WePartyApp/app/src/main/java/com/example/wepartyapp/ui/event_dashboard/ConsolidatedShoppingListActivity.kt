package com.example.wepartyapp.ui.event_dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ConsolidatedShoppingListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConsolidatedShoppingListScreenUI()
        }
    }
}

@Composable
fun ConsolidatedShoppingListScreenUI() {
    Box(
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
            Icon(                                                           //pg icon
                imageVector = Icons.Default.List,
                contentDescription = null
            )
            Text(                                                           //pg icon
                text = "Consolidated Shopping List (Coming Soon)",
                color = Color.Black,
                fontSize = 18.sp
            )
        }
    }
}