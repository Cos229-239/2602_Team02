package com.example.wepartyapp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.auth.LoginActivity
import com.example.wepartyapp.ui.calendar.CalendarScreenUI
import com.example.wepartyapp.ui.profile.DietaryPreferencesActivity
import com.google.firebase.auth.FirebaseAuth

// 1. Convert to ComponentActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

// 2. The Main Scaffold with Bottom Navigation
@Composable
fun MainScreen() {
    // 0 = Home, 1 = Calendar, 2 = Profile
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                // Home Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF4081))
                )
                // Calendar Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Calendar") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF4081))
                )
                // Profile Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF4081))
                )
            }
        }
    ) { paddingValues ->
        // This Box handles switching screens
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreenUI()
                1 -> CalendarScreenUI()
                2 -> ProfileScreenUI()
            }
        }
    }
}

// 3. HOME SCREEN UI
@Composable
fun HomeScreenUI() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(24.dp)
    ) {
        // HEADER: Logo Center + Red Balloon Logout Right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Logo Centered
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp, 100.dp)
                    .align(Alignment.Center)
            )

            // Red Balloon Logout (Top Right)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(60.dp, 75.dp)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomEnd = 30.dp, bottomStart = 4.dp))
                    .background(Color(0xFFFF1744)) // Red Color
                    .clickable {
                        // LOGOUT LOGIC
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
            ) {
                Text(
                    text = "Logout",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Welcome Text
        Text(
            text = "Welcome Home!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// 4. PROFILE SCREEN UI
@Composable
fun ProfileScreenUI() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile Coming Soon!", fontSize = 24.sp, color = Color.Black)

        Button(
            onClick = {
                // Navigate back to setup if they want to edit
                context.startActivity(Intent(context, DietaryPreferencesActivity::class.java))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Edit Profile Setup")
        }
    }
}