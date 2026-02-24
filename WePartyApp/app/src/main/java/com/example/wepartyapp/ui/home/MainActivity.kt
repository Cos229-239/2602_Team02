package com.example.wepartyapp.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person // <-- Added Person Icon
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
import androidx.compose.ui.layout.ContentScale // <-- Added for image cropping
import coil.compose.AsyncImage // <-- Added Coil for loading images
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.auth.LoginActivity
import com.example.wepartyapp.ui.calendar.CalendarScreenUI
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.wepartyapp.ui.create_event.CreateEventScreenUI
import com.example.wepartyapp.ui.event_dashboard.ConsolidatedShoppingListScreenUI
import androidx.compose.foundation.shape.CircleShape
import com.example.wepartyapp.ui.profile.DietaryPreferencesScreenUI
import com.example.wepartyapp.ui.profile.ProfileScreenUI
import com.example.wepartyapp.ui.create_event.CreateEventActivity
import androidx.lifecycle.viewmodel.compose.viewModel // <-- Added for ViewModel
import com.example.wepartyapp.ui.EventViewModel // <-- Added to import EventViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.lazy.grid.GridCells // <-- Added for LazyGrid Layout
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid// <-- Added for LazyGrid Layout
import androidx.compose.foundation.lazy.grid.items // <-- Added for LazyGrid Layout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.Shape

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

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val eventViewModel: EventViewModel = viewModel() // <-- Instantiated the ViewModel here

    Scaffold(
        modifier = Modifier.border(3.dp, color = Color.Black),
        topBar = {
            Header(
                selectedTab = selectedTab, // <-- Passed the tab so Header knows when to refresh!
                onNavigateToDietary = { selectedTab = 5 },
                onNavigateToProfile = { selectedTab = 6 },
                onNotificationClick = { selectedTab = 8 }
            )
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
                0 -> HomeScreenUI(viewModel = eventViewModel, onNotificationsClick = { selectedTab = 8 })
                1 -> CalendarScreenUI(viewModel = eventViewModel) // <-- Passed the ViewModel to fix the error!
                // 2 -> Create Event Activity Launched In Navigation Bar
                3 -> ConsolidatedShoppingListScreenUI()
//                4 -> EventsUI()
                5 -> DietaryPreferencesScreenUI( onBack = { selectedTab = 6 } )
                6 -> ProfileScreenUI(
                    onEditDietaryClick = { selectedTab = 5 },
                    onEditProfileClick = { selectedTab = 7 },
                    onEventDashboardClick = {
                        val intent = Intent(context, com.example.wepartyapp.ui.event_dashboard.EventDashboardActivity::class.java)
                        context.startActivity(intent)
                    }
                )
                7 -> com.example.wepartyapp.ui.profile.ProfileSettingsScreenUI( onBack = { selectedTab = 6 } )
                8 -> NotificationsScreenUI( onBack = { selectedTab = 0 } ) // <-- Added Notifications Screen here
            }
        }
    }
}

@Composable
fun Header(
    selectedTab: Int, // <-- Added parameter
    onNavigateToDietary: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNotificationClick: () -> Unit,
){
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Every time the user changes a tab (like returning from settings), fetch the newest picture!
    LaunchedEffect(selectedTab) {
        profilePhotoUri = FirebaseAuth.getInstance().currentUser?.photoUrl
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFB65C5C))
            .border(3.dp, color = Color.Black)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        val context = LocalContext.current
        var expanded by remember { mutableStateOf(false) }

        // - Profile (Now with the image inside!) -
        Box(
            modifier = Modifier
                .size(50.dp)
                .border(2.dp, color = Color.Black, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.CenterStart)
                .clickable { onNavigateToProfile() },
            contentAlignment = Alignment.Center // Centers the placeholder icon if no image
        ) {
            if (profilePhotoUri != null) {
                // Load the image from Firebase Storage
                AsyncImage(
                    model = profilePhotoUri,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop, // Crops the image perfectly to the circle
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show a default gray icon if they haven't uploaded an image yet
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile Icon",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Gray
                )
            }
        }

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

            // - Notification Button -
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)) // rounded square
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp) // icon size inside the square
                )
            }


//            IconButton(
//                onClick = { expanded = true }
//            ) {
//                Icon(
//                    modifier = Modifier.size(50.dp),
//                    imageVector = Icons.Default.Menu,
//                    contentDescription = "Settings",
//                    tint = Color.White
//                )
//            }
//
//            DropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false }
//            ) {
//
//                DropdownMenuItem(
//                    text = { Text("Profile") },
//                    onClick = {
//                        expanded = false
//                        onNavigateToProfile()
//                    }
//                )
//
//                DropdownMenuItem(
//                    text = { Text("Dietary Preferences") },
//                    onClick = {
//                        expanded = false
//                        // Add Screen Navigation Here (Dietary Preferences)
//                        onNavigateToDietary()
//                    }
//                )
//
//                // --- ANDY'S TEMPORARY DASHBOARD BUTTON MOVED HERE ---
//                DropdownMenuItem(
//                    text = { Text("Event Dashboard") },
//                    onClick = {
//                        expanded = false
//                        val intent = Intent(context, com.example.wepartyapp.ui.event_dashboard.EventDashboardActivity::class.java)
//                        context.startActivity(intent)
//                    }
//                )
//
//                DropdownMenuItem(
//                    text = { Text("Logout", color = Color.Red) },
//                    onClick = {
//                        expanded = false
//
//                        FirebaseAuth.getInstance().signOut()
//                        val intent = Intent(context, LoginActivity::class.java)
//                        intent.flags =
//                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        context.startActivity(intent)
//                    }
//                )
//            }
        }
    }


}

@Composable
fun HomeScreenUI(viewModel: EventViewModel, onNotificationsClick: () -> Unit) {

    val events by viewModel.events.observeAsState(emptyList())
    val today = java.time.LocalDate.now()


    val upcomingEvents = events
        .filter { event -> event.date?.let { it >= today } ?: false }
        .sortedBy { it.date }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = today.toString(),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        // - Events -
        Text(
            text = "Upcoming Events",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(upcomingEvents) { event ->
                EventCard(
                    title = event.name,
                    date = event.date?.toString() ?: "No Date"
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

}

@Composable
fun NavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val context = LocalContext.current
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
        ) {
            context.startActivity(Intent(context, CreateEventActivity::class.java))
        }

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
                .padding(20.dp)
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