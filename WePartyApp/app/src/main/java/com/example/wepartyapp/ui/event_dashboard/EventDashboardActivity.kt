package com.example.wepartyapp.ui.event_dashboard

// These imports are crucial! They fix your "unresolved reference" errors.
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PinkBackground = Color(0xFFFFE9EA)
val ButtonPink = Color(0xFFECA4A6)
val DarkText = Color(0xFF222222)

// A way to track which tab we are on
enum class DashboardTab { ITEMS, CHAT, MAP }

class EventDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventDashboardScreenUI()
        }
    }
}

@Composable
fun EventDashboardScreenUI() {
    // 'remember' keeps the state alive across screen redraws
    // We start on the ITEMS tab by default
    var currentTab by remember { mutableStateOf(DashboardTab.ITEMS) }

    // Column stacks items vertically
    Column(
        modifier = Modifier
            .fillMaxSize() // Take up the whole screen
            .background(PinkBackground)
            .padding(16.dp)
    ) {
        // --- TOP BAR (Back button + Title) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.clickable { /* TODO: Add back navigation logic later */ }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Home", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Event Dashboard",
            fontSize = 32.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            // Serif font placeholder
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- TAB BUTTONS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly // Spaces the 3 buttons out evenly
        ) {
            // Pass the current tab and a function to update it when clicked
            TabButton("Items", currentTab == DashboardTab.ITEMS) { currentTab = DashboardTab.ITEMS }
            TabButton("Chat/Feed", currentTab == DashboardTab.CHAT) { currentTab = DashboardTab.CHAT }
            TabButton("Map", currentTab == DashboardTab.MAP) { currentTab = DashboardTab.MAP }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Event Subtitle
        Text(
            text = "Valentines Day Party",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- CONTENT SWITCHER ---
        when (currentTab) {
            DashboardTab.ITEMS -> ChecklistContent()
            DashboardTab.CHAT -> ChatFeedContent()
            DashboardTab.MAP -> MapContent()
        }
    }
}

// Reusable Composable for tab buttons to keep code clean
@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(
                // If selected, make it pink. If not, make it transparent
                color = if (isSelected) ButtonPink else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = DarkText,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = DarkText)
    }
}