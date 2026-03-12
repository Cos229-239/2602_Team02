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
                //Spacer(modifier = Modifier.height(40.dp)) // <-- Pushes the whole screen down!
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
                            if (item.isNotBlank()) {
                                viewItemModel.addItems(PartyItem(name = item, price = "Loading..."))
                                viewModel.getData(item)                              //trigger api before resetting item string
                                item =
                                    ""                                            //resetting item to an empty string
                            }
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
                            val exactPrice =
                                result.data // The ViewModel now hands us the exact string directly

                            val ogList = viewItemModel._items.value
                            val mutableCopy = ogList.toMutableList()
                            val index = mutableCopy.indexOfLast { it.price == "Loading..." }
                            if (index != -1) {
                                viewItemModel.updatePrice(mutableCopy[index].name, exactPrice)
                            }
                        }

                        is NetworkResponse.Error -> {
                            val ogList = viewItemModel._items.value
                            val mutableCopy = ogList.toMutableList()
                            val index = mutableCopy.indexOfLast { it.price == "Loading..." }
                            if (index != -1) {
                                viewItemModel.updatePrice(mutableCopy[index].name, "Unavailable")
                                Toast.makeText(
                                    context,
                                    "Could not fetch price for item.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        else -> {}
                    }
                }
                val _itemList by viewItemModel._items.collectAsState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(_itemList) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = it.name)

                            // Styled price text based on its status
                            Text(
                                text = it.price,
                                color = when (it.price) {
                                    "Unavailable" -> Color.Red
                                    "Loading..." -> Color.Gray
                                    else -> Color.Black
                                }
                            )
                        }
                        Divider()
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