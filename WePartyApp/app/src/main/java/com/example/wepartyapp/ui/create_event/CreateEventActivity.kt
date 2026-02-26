package com.example.wepartyapp.ui.create_event

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.ItemPriceViewModel
import com.example.wepartyapp.ui.home.MainScreen

class CreateEventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemPriceViewModel = ViewModelProvider(this)[ItemPriceViewModel::class.java]
        val viewItemModel = ViewModelProvider(this)[EventViewModel::class.java]

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = CreateEventRoutes.createEvent) {
                composable(CreateEventRoutes.mainScreen) {
                    MainScreen()
                }
                composable(CreateEventRoutes.createEvent) {
                    CreateEventScreenUI(navController, viewItemModel)
                }
                composable(CreateEventRoutes.addItems) {
                    AddItemsScreenUI(navController, itemPriceViewModel, viewItemModel)
                }
                composable(CreateEventRoutes.inviteFriends) {
                    InviteFriendsScreenUI(navController, viewItemModel)
                }
            }
        }
    }
}

// CreateEventScreenUI.kt
@Composable
fun CreateEventScreenUI(navController: NavController, viewItemModel: EventViewModel) {
    Box(
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
                IconButton(onClick = {navController.navigate(CreateEventRoutes.mainScreen)}) {                        //back to home pg btn
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        Modifier.size(35.dp)
                    )
                }
                Text(
                    text = "Home",
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
                    imageVector = Icons.Default.Create,
                    contentDescription = null,
                    Modifier.size(60.dp),
                    tint = Color(0xFFBF6363)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(                                                           //pg title
                    text = "Create Event",
                    fontSize = 30.sp,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            EventDetailsScreenUI(viewItemModel)
        }
        Button(
            onClick = {navController.navigate(CreateEventRoutes.addItems)},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Text(text = "Next: Add Items", color = Color.Black)
        }
    }
}