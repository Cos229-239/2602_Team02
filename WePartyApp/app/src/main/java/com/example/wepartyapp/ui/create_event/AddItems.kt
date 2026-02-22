package com.example.wepartyapp.ui.create_event

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wepartyapp.ui.ItemPriceViewModel

// UI for the Add Items screen
//@Preview
@Composable
fun AddItemsScreenUI(navController: NavController) {
    var item by remember {                                              //start with an empty string
        mutableStateOf("")
    }
    var itemsList by remember {                                             //start with an empty list of strings
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {navController.navigate(CreateEventRoutes.createEvent)}) {                    //back to events btn
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
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    Modifier.size(60.dp),
                    tint = Color(0xFFBF6363)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(                                                           //pg title
                    text = "Add Items",
                    fontSize = 30.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(                                                            //add items section
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
                    label = {Text(text = "Item")}
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                    if (item.isNotBlank()) {
                        itemsList = itemsList + item                         //adding item to list when btn is clicked
                        item = ""                                           //resetting item to an empty string
                    } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
                ) {
                    Text(text = "Add", color = Color.Black)
                }
            }
            ItemListComp(itemsList = itemsList)
        }
        Button(
            onClick = {navController.navigate(CreateEventRoutes.inviteFriends)},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
            ) {
            Text(text = "Next: Invite Friends", color = Color.Black)
        }
    }
}

@Composable
fun ItemListComp(
    itemsList: List<String>,
    modifier: Modifier = Modifier
){
    LazyColumn(modifier) {
        items(itemsList) {currentItem ->
            Text(
                text = currentItem,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Divider()
        }
    }
}