package com.example.wepartyapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.wepartyapp.ui.home.MainActivity
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
                    // Navigate directly to the Main Dashboard
                    val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
                    // Clear the back stack so they can't go back to onboarding
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
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
            "Start by setting the date, time, location, and a summary. It's the first step to an unforgettable party!",
            R.drawable.weparty_create
        ),
        OnboardingItem(
            "Smart Shopping List",
            "Add items you need. We automatically look up prices and consolidate all items into one list so you see what's needed.",
            R.drawable.weparty_additems
        ),
        OnboardingItem(
            "Invite Friends",
            "Send invites instantly to your crew. Friends can then see your list and start choosing what they would like to bring to your party!.",
            R.drawable.weparty_addfriends
        ),
        OnboardingItem(
            "Calendar View",
            "Stay organized! See all your upcoming parties and events at a glance on the interactive calendar.",
            R.drawable.weparty_calendar
        )
    )

    // 3. Pager setup
    val pagerState = rememberPagerState(pageCount = { onboardingItems.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
    ) {
        // Top Row: Styled Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, end = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onFinish,
                border = BorderStroke(1.5.dp, Color(0xFFFF4081)),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Skip",
                    color = Color(0xFFFF4081),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Middle: The Swipeable Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardingPage(item = onboardingItems[pageIndex])
        }

        // Bottom Row: Dot Indicators & Next Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Dot Indicator
            PageIndicator(
                pageCount = onboardingItems.size,
                currentPage = pagerState.currentPage
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
                    color = Color.White,
                    fontWeight = FontWeight.Bold
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
                .size(500.dp)
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
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// 5. Custom Dot Indicator Component
@Composable
fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(pageCount) { index ->
            val color = if (currentPage == index) Color(0xFFFF4081) else Color.LightGray
            val width = if (currentPage == index) 24.dp else 12.dp

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}