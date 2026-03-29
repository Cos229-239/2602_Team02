package com.example.wepartyapp.ui.create_event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
import com.example.wepartyapp.utils.BaseActivity

class CreateEventActivity : BaseActivity() {
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
                    WindowCompat.setDecorFitsSystemWindows(window, false)
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

// CreateEventScreenUI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreenUI(navController: NavController, viewItemModel: EventViewModel) {

    val context = LocalContext.current

    // --- Create the Scroll State ---
    val scrollState = rememberScrollState()

    // --- Tracks if we should show red validation errors ---
    var showErrors by remember { mutableStateOf(false) }

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
                .padding(innerpadding)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // -- Back button - returns to main activity --
                    IconButton(onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            Modifier.size(30.dp)
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
                    // -- Page Icon --
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        Modifier.size(65.dp),
                        tint = Color(0xFFBF6363)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // -- Page Title --
                    Text(
                        text = "Create Event",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // --- Pass the 'showErrors' flag down to the details screen ---
                EventDetailsScreenUI(viewItemModel, showErrors)

                Spacer(modifier = Modifier.height(80.dp))
            }

            // --- Next Page Button with Dynamic Toast Error Handling ---
            Button(
                onClick = {
                    if (isFormComplete) {
                        showErrors = false // Reset errors on success
                        navController.navigate(CreateEventRoutes.addItems)
                    } else {
                        showErrors = true // Flip the flag to true to trigger red text fields

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
                border = if (isFormComplete) BorderStroke(1.dp, Color(0xFFBF6363)) else BorderStroke(1.dp, Color.DarkGray),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormComplete) Color(0xFFFA8989) else Color.LightGray),
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