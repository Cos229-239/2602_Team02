package com.example.wepartyapp.ui.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.R

@Composable
fun DietaryPreferencesScreenUI(onBack: () -> Unit) {
    // 1. Grab the context and the SharedPreferences file
    val context = LocalContext.current
    val sharedPref = remember {
        context.getSharedPreferences("DietaryPrefs", Context.MODE_PRIVATE)
    }

    // 2. Initialize variables by reading from SharedPreferences
    // The second parameter (e.g., 'true' or 'false') is the default fallback if no save exists yet
    var noOnions by remember { mutableStateOf(sharedPref.getBoolean("noOnions", true)) }
    var noKetchup by remember { mutableStateOf(sharedPref.getBoolean("noKetchup", true)) }
    var extraMayo by remember { mutableStateOf(sharedPref.getBoolean("extraMayo", true)) }
    var glutenFree by remember { mutableStateOf(sharedPref.getBoolean("glutenFree", false)) }

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

        // BOTTOM: Navigation & Save
        Button(
            onClick = {
                // 3. Write the current toggle states to SharedPreferences
                with(sharedPref.edit()) {
                    putBoolean("noOnions", noOnions)
                    putBoolean("noKetchup", noKetchup)
                    putBoolean("extraMayo", extraMayo)
                    putBoolean("glutenFree", glutenFree)
                    apply() // apply() saves it asynchronously in the background
                }

                // 4. Navigate back to the Home screen
                onBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(50.dp)
        ) {
            Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
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