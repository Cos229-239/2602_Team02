package com.example.wepartyapp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.auth.LoginActivity
import com.example.wepartyapp.ui.calendar.CalendarScreenUI
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    // 0 = Home, 1 = Calendar, 2 = Profile
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            // The Navigation Bar is now ALWAYS visible on every screen
            NavigationBar(containerColor = Color.White) {
                // Home Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF4081),
                        indicatorColor = Color(0xFFFFE9EA)
                    )
                )
                // Calendar Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Calendar") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF4081),
                        indicatorColor = Color(0xFFFFE9EA)
                    )
                )
                // Profile Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") },
                    // Keep the Profile icon highlighted even if they are in the Dietary sub-screen!
                    selected = selectedTab == 2 || selectedTab == 3,
                    onClick = { selectedTab = 2 }, // Clicking this while in Dietary takes you back to main Profile
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF4081),
                        indicatorColor = Color(0xFFFFE9EA)
                    )
                )
            }
        }
    ) { paddingValues ->
        // This Box handles switching screens
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreenUI()
                1 -> CalendarScreenUI()
                2 -> ProfileScreenUI(onEditClick = { selectedTab = 3 })
                3 -> DietaryPreferencesScreenUI(onBack = { selectedTab = 2 })
            }
        }
    }
}

// --- SCREEN 0: HOME ---
@Composable
fun HomeScreenUI() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFFE9EA)).padding(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp, 100.dp).align(Alignment.Center)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(60.dp, 75.dp)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomEnd = 30.dp, bottomStart = 4.dp))
                    .background(Color(0xFFFF1744))
                    .clickable {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
            ) {
                Text("Logout", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            text = "Welcome Home!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// --- SCREEN 2: PROFILE (MAIN) ---
@Composable
fun ProfileScreenUI(onEditClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFFE9EA)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile Coming Soon!", fontSize = 24.sp, color = Color.Black)

        Button(
            onClick = onEditClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Edit Profile Setup", color = Color.White)
        }
    }
}

// --- SCREEN 3: DIETARY PREFERENCES (SUB-SCREEN) ---
@Composable
fun DietaryPreferencesScreenUI(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
    ) {
        // Just the text, centered on the screen, exactly as requested
        Text(
            text = "Dietary Preferences\n(Coming Soon)",
            fontSize = 20.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}