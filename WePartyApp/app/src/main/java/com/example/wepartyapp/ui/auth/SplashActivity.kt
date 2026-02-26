package com.example.wepartyapp.ui.auth

import android.app.Activity // <-- Added
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect // <-- Added
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView // <-- Added
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat // <-- Added
import com.example.wepartyapp.R
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // --- Status Bar Fix ---
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }

            SplashScreenUI(
                onTimeout = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@Composable
fun SplashScreenUI(onTimeout: () -> Unit) {
    LaunchedEffect(key1 = true) {
        delay(3000)
        onTimeout()
    }

    // --- THE HOPPING ANIMATION ---
    val infiniteTransition = rememberInfiniteTransition(label = "hop")

    // 1. Side to Side (Horizontal)
    // Takes 900ms to go from Left to Right
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -40f, // Start Left
        targetValue = 40f,   // End Right
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hopX"
    )

    // 2. Up and Down (Vertical)
    // Takes 450ms to go Up.
    // Total time (Up + Down) = 900ms. Perfect sync!
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,   // Ground
        targetValue = -50f,  // Air (Peak of jump)
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hopY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
    ) {

        // CENTER COLUMN: Hopping Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "WeParty Logo",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .offset(x = offsetX.dp, y = offsetY.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Plan. Party. Repeat.",
                color = Color(0xFF333333),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // BOTTOM COLUMN: Team Names
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "By: Rodney Ford\nLesly Morales\nMaret Rivera Merced\nAndy Tran\nHasani Carter",
                color = Color(0xFF333333),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}