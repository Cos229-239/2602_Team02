package com.example.wepartyapp.ui.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // <-- Added for scrolling
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll // <-- Added for scrolling
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

    // Custom Modifications
    var noOnions by remember { mutableStateOf(sharedPref.getBoolean("noOnions", true)) }
    var noKetchup by remember { mutableStateOf(sharedPref.getBoolean("noKetchup", true)) }
    var noMushrooms by remember { mutableStateOf(sharedPref.getBoolean("noMushrooms", true)) }
    var extraMayo by remember { mutableStateOf(sharedPref.getBoolean("extraMayo", true)) }

    // Allergies & Intolerances
    var glutenFree by remember { mutableStateOf(sharedPref.getBoolean("glutenFree", false)) }
    var dairyFree by remember { mutableStateOf(sharedPref.getBoolean("dairyFree", false)) }
    var nutAllergy by remember { mutableStateOf(sharedPref.getBoolean("nutAllergy", false)) }
    var shellfishAllergy by remember { mutableStateOf(sharedPref.getBoolean("shellfishAllergy", false)) }

    // Diets & Lifestyles
    var vegetarian by remember { mutableStateOf(sharedPref.getBoolean("vegetarian", false)) }
    var vegan by remember { mutableStateOf(sharedPref.getBoolean("vegan", false)) }
    var halal by remember { mutableStateOf(sharedPref.getBoolean("halal", false)) }
    var keto by remember { mutableStateOf(sharedPref.getBoolean("keto", false)) }

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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ensures the middle section takes up available space
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Dietary Preferences",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF4081)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Select all that apply to your profile. This helps hosts plan the menu!",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Wrap the toggles in a scrollable column so it doesn't break small screens
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- CATEGORY 1: Custom Modifications ---
                Text(
                    text = "Custom Modifications",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB65C5C),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                )
                PreferenceToggle("No Onions", noOnions) { noOnions = it }
                PreferenceToggle("No Ketchup", noKetchup) { noKetchup = it }
                PreferenceToggle("No Mushrooms", noMushrooms) { noMushrooms = it }
                PreferenceToggle("Extra Mayo", extraMayo) { extraMayo = it }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)

                // --- CATEGORY 2: Allergies & Intolerances ---
                Text(
                    text = "Allergies & Intolerances",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB65C5C),
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                )
                PreferenceToggle("Gluten Free", glutenFree) { glutenFree = it }
                PreferenceToggle("Dairy Free", dairyFree) { dairyFree = it }
                PreferenceToggle("Nut Allergy", nutAllergy) { nutAllergy = it }
                PreferenceToggle("Shellfish Allergy", shellfishAllergy) { shellfishAllergy = it }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)

                // --- CATEGORY 3: Diets & Lifestyles ---
                Text(
                    text = "Diets & Lifestyles",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB65C5C),
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                )
                PreferenceToggle("Vegetarian", vegetarian) { vegetarian = it }
                PreferenceToggle("Vegan", vegan) { vegan = it }
                PreferenceToggle("Halal", halal) { halal = it }
                PreferenceToggle("Keto", keto) { keto = it }
            }
        }

        // BOTTOM: Navigation & Save
        Button(
            onClick = {
                // 3. Write the current toggle states to SharedPreferences
                with(sharedPref.edit()) {
                    putBoolean("noOnions", noOnions)
                    putBoolean("noKetchup", noKetchup)
                    putBoolean("noMushrooms", noMushrooms)
                    putBoolean("extraMayo", extraMayo)

                    putBoolean("glutenFree", glutenFree)
                    putBoolean("dairyFree", dairyFree)
                    putBoolean("nutAllergy", nutAllergy)
                    putBoolean("shellfishAllergy", shellfishAllergy)

                    putBoolean("vegetarian", vegetarian)
                    putBoolean("vegan", vegan)
                    putBoolean("halal", halal)
                    putBoolean("keto", keto)
                    apply() // apply() saves it asynchronously in the background
                }

                // 4. Navigate back to the Home screen
                onBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp) // Made button full width to match other screens
        ) {
            Text("Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

// Reusable component for the toggle rows
@Composable
fun PreferenceToggle(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp), // Tightened vertical padding slightly for the longer list
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