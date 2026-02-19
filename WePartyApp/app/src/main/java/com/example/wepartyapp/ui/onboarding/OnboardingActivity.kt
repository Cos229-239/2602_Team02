package com.example.wepartyapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.profile.DietaryPreferencesActivity
import kotlinx.coroutines.launch

// 1. A simple data class to hold our page information
data class OnboardingItem(
    val title: String,
    val description: String,
    val imageRes: Int
)

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OnboardingScreenUI(
                onFinish = {
                    // Go to Profile Setup when done or skipped
                    startActivity(Intent(this, DietaryPreferencesActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@Composable
fun OnboardingScreenUI(onFinish: () -> Unit) {
    // 2. Define our 4 slides
    val onboardingItems = listOf(
        OnboardingItem(
            "Create Your Party",
            "Start by setting the date, time, and location. It's the first step to an unforgettable event!",
            R.drawable.app_logo // Using your logo as placeholder for now
        ),
        OnboardingItem(
            "Invite Friends",
            "Send invites instantly to your crew. Track who is coming with a real-time guest list.",
            R.drawable.app_logo
        ),
        OnboardingItem(
            "Smart Shopping List",
            "Add items you need. We automatically consolidate everyone's requests into one master list so you buy exactly what's needed.",
            R.drawable.app_logo
        ),
        OnboardingItem(
            "Calendar View",
            "Stay organized! See all your upcoming parties and events at a glance on the interactive calendar.",
            R.drawable.app_logo
        )
    )

    // 3. Pager setup
    val pagerState = rememberPagerState(pageCount = { onboardingItems.size })
    val coroutineScope = rememberCoroutineScope() // Needed to animate button clicks

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
    ) {
        // Top Row: Skip Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinish) {
                Text("Skip", color = Color(0xFFFF4081), fontSize = 16.sp)
            }
        }

        // Middle: The Swipeable Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardingPage(item = onboardingItems[pageIndex])
        }

        // Bottom Row: Indicator & Next Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "1/4" Indicator
            Text(
                text = "${pagerState.currentPage + 1}/${onboardingItems.size}",
                color = Color(0xFFFF4081),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Next / Get Started Button
            Button(
                onClick = {
                    if (pagerState.currentPage < onboardingItems.size - 1) {
                        // Swipe to next page
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        // Finish onboarding
                        onFinish()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
            ) {
                Text(
                    text = if (pagerState.currentPage == onboardingItems.size - 1) "Get Started" else "Next",
                    color = Color.White
                )
            }
        }
    }
}

// 4. This tells Compose how to draw ONE single page
@Composable
fun OnboardingPage(item: OnboardingItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.title,
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 32.dp)
        )

        Text(
            text = item.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF4081),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = item.description,
            fontSize = 18.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}