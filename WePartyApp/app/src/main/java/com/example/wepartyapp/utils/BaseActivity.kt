package com.example.wepartyapp.utils

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.wepartyapp.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : ComponentActivity() {

    companion object {
        var lastInteractionTime = System.currentTimeMillis()
    }

    private val timeoutHandler = Handler(Looper.getMainLooper())

    // Set to 10 minutes
    private val TIMEOUT_DELAY = 10 * 60 * 1000L

    private val timeoutRunnable = Runnable {
        performLogout()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastInteractionTime = System.currentTimeMillis()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        lastInteractionTime = System.currentTimeMillis()
        resetDisconnectTimer()
    }

    override fun onResume() {
        super.onResume()

        val timeAsleep = System.currentTimeMillis() - lastInteractionTime

        if (timeAsleep > TIMEOUT_DELAY) {
            performLogout()
        } else {
            resetDisconnectTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        stopDisconnectTimer()
    }

    private fun resetDisconnectTimer() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_DELAY)
    }

    private fun stopDisconnectTimer() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
    }

    private fun performLogout() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseAuth.getInstance().signOut()

            Toast.makeText(this, "Logged out due to inactivity", Toast.LENGTH_LONG).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}