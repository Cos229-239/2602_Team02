package com.example.wepartyapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wepartyapp.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Wait for 3 seconds (3000ms), then move to Login
        Handler(Looper.getMainLooper()).postDelayed({

            // 1. Create the "Intent" (The navigation command)
            val intent = Intent(this, LoginActivity::class.java)

            // 2. Start the Login Activity
            startActivity(intent)

            // 3. Close the Splash Activity so the user can't go back to it
            finish()

        }, 3000)
    }
}