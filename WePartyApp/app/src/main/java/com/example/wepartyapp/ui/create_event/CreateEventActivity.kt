package com.example.wepartyapp.ui.create_event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast // <-- Added for the graceful error popup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect // <-- Added for status bar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView // <-- Added for status bar
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat // <-- Added for status bar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.ItemPriceViewModel
import com.example.wepartyapp.ui.home.MainActivity
import com.example.wepartyapp.ui.home.MainScreen

class CreateEventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemPriceViewModel = ViewModelProvider(this)[ItemPriceViewModel::class.java]
        val viewItemModel = ViewModelProvider(this)[EventViewModel::class.java]

        setContent {
            // --- Status Bar Fix ---
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreenUI(navController: NavController, viewItemModel: EventViewModel) {
    val context = LocalContext.current // <-- Grab context for the Intent

    // --- Validation Logic for all 4 required fields ---
    val isFormComplete = viewItemModel.eventName.isNotBlank() &&
            viewItemModel.eventDate.isNotBlank() &&
            viewItemModel.eventTime.isNotBlank() &&
            viewItemModel.eventAddress.isNotBlank()
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
        Box(
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
                    IconButton(onClick = {
                        // Explicitly return to Main Activity and kill this one
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }) {
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
                    Text(
                        //pg title
                        text = "Create Event",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Pass the viewItemModel to EventDetailsScreenUI so it caches data
                EventDetailsScreenUI(viewItemModel)
            }

            // --- Updated Button with Dynamic Toast Error Handling ---
            Button(
                onClick = {
                    if (isFormComplete) {
                        navController.navigate(CreateEventRoutes.addItems)
                    } else {
                        // Identify specifically what is missing
                        val missing = mutableListOf<String>()
                        if (viewItemModel.eventName.isBlank()) missing.add("Name")
                        if (viewItemModel.eventDate.isBlank()) missing.add("Date")
                        if (viewItemModel.eventTime.isBlank()) missing.add("Time")
                        if (viewItemModel.eventAddress.isBlank()) missing.add("Address")

                        val toastMessage = "Missing: ${missing.joinToString(", ")}"
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                // Logic: Button remains clickable but turns gray to hint it is incomplete
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormComplete) Color(0xFFFA8989) else Color.LightGray
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                Text(
                    text = "Next: Add Items",
                    color = if (isFormComplete) Color.Black else Color.DarkGray
                )
            }
        }
    }
}