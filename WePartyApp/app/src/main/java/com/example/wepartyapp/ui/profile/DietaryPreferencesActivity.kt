package com.example.wepartyapp.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.R

@Composable
fun DietaryPreferencesScreenUI(onBack: () -> Unit) {
    // State variables for the toggle switches
    var noOnions by remember { mutableStateOf(true) }
    var noKetchup by remember { mutableStateOf(true) }
    var extraMayo by remember { mutableStateOf(true) }
    var glutenFree by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP: Logo
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(top = 16.dp)
        )

        // MIDDLE: Content & Toggles
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Dietary Preferences",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF4081)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Testing",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Custom Toggle Switches
            PreferenceToggle("No Onions", noOnions) { noOnions = it }
            PreferenceToggle("No Ketchup", noKetchup) { noKetchup = it }
            PreferenceToggle("Extra Mayo", extraMayo) { extraMayo = it }
            PreferenceToggle("Gluten Free", glutenFree) { glutenFree = it }
        }

        // BOTTOM: Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFF4081)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (index == 0) Color(0xFFFF4081) else Color.LightGray)
                    )
                }
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Reusable component for the toggle rows
@Composable
fun PreferenceToggle(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFF4081),
                checkedTrackColor = Color(0xFFFF4081).copy(alpha = 0.5f)
            )
        )
    }
}