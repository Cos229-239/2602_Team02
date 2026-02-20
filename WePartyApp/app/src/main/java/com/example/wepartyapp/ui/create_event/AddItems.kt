package com.example.wepartyapp.ui.create_event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// UI for the Add Items screen
@Composable
fun AddItemsScreenUI() {
    var item by remember {                                              //start with an empty string
        mutableStateOf("")
    }
    var items by remember {                                             //start with an empty list of strings
        mutableStateOf(listOf<String>())
    }

    Box(                                                                //outer most layer
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
            IconButton(onClick = {})                                        //back to events btn
            {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Create Event"
                )
            }
            Icon(                                                           //pg icon
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null
            )
            Text(                                                           //pg title
                text = "Add Items",
                color = Color.Black,
                fontSize = 18.sp
            )
            Row(                                                            //add items section
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = item,
                    onValueChange = {},
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {}) {
                    Text(text = "Add")
                }
            }
        }
    }
}