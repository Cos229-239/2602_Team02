package com.example.wepartyapp.ui.create_event

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.ItemPriceViewModel
import com.example.wepartyapp.ui.api.NetworkResponse
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.PartyItem

// UI for the Add Items screen
@Composable
fun AddItemsScreenUI(navController: NavController, viewModel: ItemPriceViewModel, viewItemModel: EventViewModel) {

    var item by remember {                                                  //start with an empty string
        mutableStateOf("")
    }

    val priceResult = viewModel.priceResult.observeAsState()
    val context = LocalContext.current // Added so we can show Toast messages

    // --- Check if at least one item is added ---
    val _itemList by viewItemModel._items.collectAsState()
    val isListNotEmpty = _itemList.isNotEmpty()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFC96B6B),
                                Color(0xFFB65C5C),
                                Color(0xFF8E3F3F)
                            )
                        )
                    )
                    .border(3.dp, color = Color.Black)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                )
            }
        }
    ) { innerpadding ->
        Box(                                                                    //outer most layer
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFE9EA))
                .padding(innerpadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {           //back to events btn
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            Modifier.size(35.dp)
                        )
                    }
                    Text(
                        text = "Create Event",
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(                                                           //pg icon
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        Modifier.size(70.dp),
                        tint = Color(0xFFBF6363)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(                                                           //pg title
                        text = "Add Items",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(                                                               //add items section
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = item,
                        onValueChange = { text ->
                            item = text
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text(text = "Item") }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            // --- Instantly force the input to lowercase and trim spaces ---
                            val trimmedItem = item.trim().lowercase()

                            // --- Check for empty input ---
                            if (trimmedItem.isBlank()) {
                                Toast.makeText(context, "Please type an item name first.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // --- Check for duplicates ---
                            // ignoreCase = true prevents adding "Chips" if "chips" is already there
                            val alreadyExists = _itemList.any { it.name.equals(trimmedItem, ignoreCase = true) }
                            if (alreadyExists) {
                                Toast.makeText(context, "That item is already on the list!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // If it passes the checks, add it
                            viewItemModel.addItems(PartyItem(name = trimmedItem, price = "Loading..."))
                            viewModel.getData(trimmedItem)
                            item = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
                    ) {
                        Text(text = "Add", color = Color.Black)
                    }
                }
                //LaunchedEffect is used to run suspendable side effects - coroutine
                LaunchedEffect(priceResult.value) {
                    when (val result = priceResult.value) {
                        is NetworkResponse.Success -> {
                            // --- We now use the Pair from the ViewModel ---
                            val itemName = result.data.first
                            val exactPrice = result.data.second

                            viewItemModel.updatePrice(itemName, exactPrice)
                        }

                        is NetworkResponse.Error -> {
                            // --- Safely display an error toast instead of corrupting the list ---
                            Toast.makeText(context, "Could not fetch price.", Toast.LENGTH_SHORT).show()
                        }

                        else -> {}
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(_itemList, key = { it.name }) { currentItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Item Details Column
                            Column(modifier = Modifier.weight(1f)) {
                                // Capitalize just the first letter for UI presentation
                                val displayName = currentItem.name.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase() else it.toString()
                                }
                                Text(text = displayName, fontSize = 16.sp, fontWeight = FontWeight.Medium)

                                // Styled price text based on its status
                                Text(
                                    text = currentItem.price,
                                    fontSize = 14.sp,
                                    color = when (currentItem.price) {
                                        "Unavailable", "Not Found" -> Color.Red
                                        "Loading..." -> Color.Gray
                                        else -> Color(0xFFBF6363)
                                    }
                                )
                            }

                            // --- Delete Button ---
                            IconButton(onClick = {
                                viewItemModel.removeItem(currentItem)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Item",
                                    tint = Color.Gray
                                )
                            }
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }

            // --- Next Button with Error Handling ---
            Button(
                onClick = {
                    if (isListNotEmpty) {
                        navController.navigate(CreateEventRoutes.inviteFriends)
                    } else {
                        // Graceful error handling: Tell the user they need at least one item
                        Toast.makeText(
                            context,
                            "Please add at least one item to your list!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                // Manually swapping colors to show a "disabled" state if list is empty
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListNotEmpty) Color(0xFFFA8989) else Color.LightGray
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Next: Invite Friends",
                    color = if (isListNotEmpty) Color.Black else Color.DarkGray
                )
            }
        }
    }
}