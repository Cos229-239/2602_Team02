package com.example.wepartyapp.ui.event_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class PartyItem(val name: String, var isChecked: Boolean = false)

@Composable
fun ChecklistContent() {
    // mutableStateListOf creates an observable list. If an item changes, the UI updates automatically
    val itemsList = remember {
        mutableStateListOf(
            PartyItem("Oreos"),
            PartyItem("Doritos"),
            PartyItem("Strawberry ice cream"),
            PartyItem("Party decorations"),
            PartyItem("M&Ms")
        )
    }

    // Box lets us overlap things. We use it to pin the "Add Items" button to the bottom.
    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.padding(bottom = 60.dp) // Leave space for the button at the bottom
        ) {
            // Iterate through our list of items
            items(itemsList) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { isChecked ->
                            // When clicked, we find the item in the list and update its state
                            val index = itemsList.indexOf(item)
                            itemsList[index] = itemsList[index].copy(isChecked = isChecked)
                        }
                    )
                    Text(text = item.name, modifier = Modifier.padding(start = 8.dp))
                }
                // Adds line under each item
                Divider(color = Color.Gray, thickness = 0.5.dp)
            }
        }

        // --- ADD ITEMS BUTTON ---
        Button(
            onClick = { /* TODO: Add logic to add a new item */ },
            colors = ButtonDefaults.buttonColors(containerColor = ButtonPink),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
        ) {
            Text("Add Items", color = Color.Black)
        }
    }
}