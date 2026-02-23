package com.example.wepartyapp.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class PasswordRecoveryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PasswordRecoveryScreenUI(
                onBackToLogin = { finish() }
            )
        }
    }
}

@Composable
fun PasswordRecoveryScreenUI(
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Password Recovery",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF4081),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        Button(
            onClick = {
                val cleanedEmail = email.trim()

                if (cleanedEmail.isEmpty()) {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(cleanedEmail).matches()) {
                    Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                auth.sendPasswordResetEmail(cleanedEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Reset email sent!", Toast.LENGTH_SHORT).show()
                        } else {
                            // This is the REAL reason it failed
                            val ex = task.exception
                            val message = when (ex) {
                                is FirebaseAuthInvalidUserException ->
                                    "No account found for that email."
                                is FirebaseAuthInvalidCredentialsException ->
                                    "That email address is not valid."
                                else ->
                                    ex?.localizedMessage ?: "Error sending reset email."
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
        ) {
            Text(text = "Send Reset Email", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Back to Login",
            color = Color(0xFFFF4081),
            fontSize = 16.sp,
            modifier = Modifier.clickable { onBackToLogin() }
        )
    }
}