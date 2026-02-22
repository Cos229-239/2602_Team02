package com.example.wepartyapp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.wepartyapp.ui.create_event.CreateEventScreenUI
import com.example.wepartyapp.ui.event_dashboard.ConsolidatedShoppingListScreenUI
import com.example.wepartyapp.ui.profile.DietaryPreferencesActivity


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

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.border(3.dp, color = Color.Black),
        topBar = {
            Header()
        },
        bottomBar = {
            NavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->

        // - This Box Contains Screen UI Content -
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFE9EA))
        ) {
            when (selectedTab) {
                // *Add Screens In Here With Corresponding Tabs*
                0 -> HomeScreenUI()
                1 -> CalendarScreenUI()
                2 -> CreateEventScreenUI()
                3 -> ConsolidatedShoppingListScreenUI()
//                4 -> EventsUI()
            }
        }
    }
}

@Composable
fun Header(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFB65C5C))
            .border(3.dp, color = Color.Black)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        val context = LocalContext.current
        var expanded by remember { mutableStateOf(false) }

        // - Profile -
        Box(
            modifier = Modifier
                .size(50.dp)
                .border(2.dp, color = Color.Black, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.CenterStart),
        )

        // - Logo -
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.Center)
        )

        // - Settings Menu -
        Box(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {

            IconButton(
                onClick = { expanded = true }
            ) {
                Icon(
                    modifier = Modifier.size(50.dp),
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                DropdownMenuItem(
                    text = { Text("Dietary Preferences") },
                    onClick = {
                        expanded = false
                        // Add Screen Navigation Here (Dietary Preferences)
                        val intent = Intent(context, DietaryPreferencesActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Logout", color = Color.Red) },
                    onClick = {
                        expanded = false

                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    // - Add date and notifications button -
}

@Composable
fun HomeScreenUI(){
    Column(
        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 50.dp)
    ) {

        Text(
            text = "Upcoming Events",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EventCard("Valentine's Day Party", "Feb 20, 2026")
            EventCard("Bob's Birthday", "March 9, 2026")
        }

        Spacer(modifier = Modifier.height(12.dp))

        EventCard("Ryan's Wedding", "April 10, 2026")
    }

    Spacer(modifier = Modifier.height(32.dp))

}

@Composable
fun NavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFFB65C5C))
            .border(3.dp, Color.Black),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // - Home -
        NavigationItem(
            icon = Icons.Default.Home,
            label = "Home",
            selected = selectedTab == 0
        ) { onTabSelected(0) }

        // - Calendar -
        NavigationItem(
            icon = Icons.Default.DateRange,
            label = "Calendar",
            selected = selectedTab == 1
        ) { onTabSelected(1) }

        // - Create Event -
        NavigationItem(
            icon = Icons.Default.Add,
            label = "Create Event",
            selected = selectedTab == 2
        ) { onTabSelected(2) }

        // - Consolidated Lists -
        NavigationItem(
            icon = Icons.Default.CheckCircle,
            label = "Lists",
            selected = selectedTab == 3
        ) { onTabSelected(3) }

        // - Events -
        NavigationItem(
            icon = Icons.Default.Edit,
            label = "Events",
            selected = selectedTab == 4
        ) { onTabSelected(4) }
    }
}
@Composable
fun NavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color.Black,
            modifier = Modifier.size(22.dp)
        )

        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) Color.White else Color.Black
        )
    }
}

@Composable
fun EventCard(title: String, date: String) {

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE57373)
        )
    ) {

        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(title)
            Text(date, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// 2. The Main Scaffold with Bottom Navigation
//@Composable
//fun MainScreen() {
//    // 0 = Home, 1 = Calendar, 2 = Profile
//    var selectedTab by remember { mutableIntStateOf(0) }
//
//    Scaffold(
//        bottomBar = {
//            NavigationBar(containerColor = Color.White) {
//                // Home Tab
//                NavigationBarItem(
//                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
//                    label = { Text("Home") },
//                    selected = selectedTab == 0,
//                    onClick = { selectedTab = 0 },
//                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF4081))
//                )
//                // Calendar Tab
//                NavigationBarItem(
//                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
//                    label = { Text("Calendar") },
//                    selected = selectedTab == 1,
//                    onClick = { selectedTab = 1 },
//                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF4081))
//                )
//                // Profile Tab
//                NavigationBarItem(
//                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
//                    label = { Text("Profile") },
//                    selected = selectedTab == 2,
//                    onClick = { selectedTab = 2 },
//                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF4081))
//                )
//            }
//        }
//    ) { paddingValues ->
//        // This Box handles switching screens
//        Box(modifier = Modifier.padding(paddingValues)) {
//            when (selectedTab) {
//                0 -> HomeScreenUI()
//                1 -> CalendarScreenUI()
//                2 -> ProfileScreenUI()
//            }
//        }
//    }
//}

// 4. PROFILE SCREEN UI
//@Composable
//fun ProfileScreenUI() {
//    val context = LocalContext.current
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFFFE9EA)),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Profile Coming Soon!", fontSize = 24.sp, color = Color.Black)
//
//        Button(
//            onClick = {
//                // Navigate back to setup if they want to edit
//                context.startActivity(Intent(context, DietaryPreferencesActivity::class.java))
//            },
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
//            modifier = Modifier.padding(top = 16.dp)
//        ) {
//            Text("Edit Profile Setup")
//        }
//    }
//}
