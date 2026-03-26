package com.example.wepartyapp.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.home.MainActivity
import com.example.wepartyapp.ui.onboarding.OnboardingActivity // <-- ADDED IMPORT
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.FragmentActivity

class LoginActivity : FragmentActivity() {

    companion object {
        var isColdBoot = true
    }

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    public override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener {
                if (currentUser.isEmailVerified) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    auth.signOut()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (isColdBoot) {
            auth.signOut()
            isColdBoot = false
        }

        setContent {
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }

            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            // State for the Google Username Dialog
            var showUsernameDialog by remember { mutableStateOf(false) }
            var pendingGoogleUser by remember { mutableStateOf<FirebaseUser?>(null) }

            val googleSignInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)!!

                        firebaseAuthWithGoogle(account.idToken!!) { success, errorMsg, isNewUser, user ->
                            isLoading = false
                            if (success) {
                                if (isNewUser && user != null) {
                                    // Pop the dialog to pick a username
                                    pendingGoogleUser = user
                                    showUsernameDialog = true
                                } else {
                                    // Existing user, send them straight in
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                            } else {
                                errorMessage = errorMsg
                            }
                        }
                    } catch (e: ApiException) {
                        isLoading = false
                        errorMessage = "Google sign-in failed."
                    }
                } else {
                    isLoading = false
                }
            }

            // --- The Username Selection Dialog ---
            if (showUsernameDialog && pendingGoogleUser != null) {
                var usernameInput by remember { mutableStateOf("") }
                var dialogError by remember { mutableStateOf<String?>(null) }
                var isDialogLoading by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { /* Locked. They must pick or cancel. */ },
                    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                    title = { Text("Pick Your Username") },
                    text = {
                        Column {
                            Text("Since this is your first time logging in with Google, please choose a unique UserID.")
                            Spacer(Modifier.height(12.dp))
                            TextField(
                                value = usernameInput,
                                onValueChange = { usernameInput = it },
                                placeholder = { Text("e.g. partyanimal99") },
                                singleLine = true
                            )
                            if (dialogError != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(dialogError!!, color = Color.Red, fontSize = 14.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val chosenId = usernameInput.trim().replace(" ", "").replace("@", "").lowercase()
                                if (chosenId.isEmpty()) {
                                    dialogError = "Username cannot be empty."
                                    return@Button
                                }
                                isDialogLoading = true
                                dialogError = null

                                // 1. Check if the username is taken
                                db.collection("UserId_Lookup").document(chosenId).get()
                                    .addOnSuccessListener { doc ->
                                        if (doc.exists()) {
                                            isDialogLoading = false
                                            dialogError = "That username is already taken."
                                        } else {
                                            // 2. Build the profile if it's available
                                            val userMap = hashMapOf(
                                                "uid" to pendingGoogleUser!!.uid,
                                                "name" to (pendingGoogleUser!!.displayName ?: "Party Animal"),
                                                "appUserId" to chosenId,
                                                "phoneNumber" to "",
                                                "email" to (pendingGoogleUser!!.email ?: ""),
                                                "friends" to emptyList<String>(),
                                                "friendRequests" to emptyList<String>()
                                            )

                                            db.collection("users").document(pendingGoogleUser!!.uid).set(userMap)
                                                .addOnSuccessListener {
                                                    db.collection("UserId_Lookup").document(chosenId)
                                                        .set(mapOf("uid" to pendingGoogleUser!!.uid))
                                                        .addOnSuccessListener {
                                                            isDialogLoading = false
                                                            showUsernameDialog = false
                                                            // --- Route to Onboarding ---
                                                            startActivity(Intent(this@LoginActivity, OnboardingActivity::class.java))
                                                            finish()
                                                        }
                                                }
                                        }
                                    }
                            },
                            enabled = !isDialogLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                        ) {
                            if (isDialogLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            else Text("Submit", color = Color.White)
                        }
                    },
                    dismissButton = {
                        if (!isDialogLoading) {
                            TextButton(onClick = {
                                isDialogLoading = true // Lock the UI while deleting

                                // Completely delete the Google Auth account if they cancel
                                pendingGoogleUser?.delete()?.addOnCompleteListener {
                                    auth.signOut()
                                    showUsernameDialog = false
                                    pendingGoogleUser = null
                                    isDialogLoading = false
                                    errorMessage = "Account creation cancelled."
                                }
                            }) {
                                Text("Cancel", color = Color.Gray)
                            }
                        }
                    }
                )
            }

            LoginScreenUI(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLoginClick = { emailInput, passwordInput ->
                    val email = emailInput.trim()
                    val password = passwordInput.trim()

                    errorMessage = null

                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please enter both email and password."
                        return@LoginScreenUI
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Please enter a valid email."
                        return@LoginScreenUI
                    }

                    isLoading = true

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            isLoading = false

                            if (task.isSuccessful) {
                                val user = auth.currentUser

                                if (user != null && user.isEmailVerified) {
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finish()
                                } else {
                                    auth.signOut()
                                    errorMessage = "Please verify your email before logging in. Check your inbox!"
                                }
                            } else {
                                val exceptionMsg = task.exception?.message ?: ""
                                errorMessage = when {
                                    exceptionMsg.contains("INVALID_LOGIN_CREDENTIALS") -> "Incorrect email or password. Please try again."
                                    exceptionMsg.contains("network error", ignoreCase = true) -> "Network error. Please check your connection."
                                    exceptionMsg.contains("blocked", ignoreCase = true) -> "Account temporarily disabled due to too many failed attempts."
                                    else -> "Authentication failed. Please try again."
                                }
                            }
                        }
                },
                onGoogleSignInClick = {
                    // --- Show Biometric Prompt before Google Sign-In ---
                    showBiometricPrompt {
                        isLoading = true
                        errorMessage = null

                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()

                        val googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                onNavigateToSignUp = {
                    startActivity(Intent(this, SignUpActivity::class.java))
                },
                onNavigateToForgotPassword = {
                    startActivity(Intent(this, PasswordRecoveryActivity::class.java))
                }
            )
        }
    }

    // --- Helper Function for Biometrics ---
    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your fingerprint or Face ID to sign in")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean, String?, Boolean, FirebaseUser?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true
                    val user = auth.currentUser

                    onResult(true, null, isNewUser, user)
                } else {
                    onResult(false, task.exception?.message ?: "Authentication failed.", false, null)
                }
            }
    }
}

@Composable
fun LoginScreenUI(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: (String, String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Logo
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "WeParty Logo",
            modifier = Modifier
                .size(180.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Welcome Back!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF4081),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email") },
            // Set keyboard to show "Next" instead of enter
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            // Move focus down to password field when "Next" is hit
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
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

        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            // Set keyboard to show "Done" instead of enter
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            // Submits the form when "Done" is hit
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // Hide keyboard
                    if (!isLoading) onLoginClick(email, password) // Only click if not already loading
                }
            ),
            trailingIcon = { // <-- The eye icon to toggle visibility
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        // Forgot Password link
        Text(
            text = "Forgot password?",
            color = Color(0xFFFF4081),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 16.dp)
                .clickable { onNavigateToForgotPassword() }
        )

        // --- In-UI Error Message Display ---
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // --- Button with Loading State ---
        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Log In", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGoogleSignInClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(50)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            enabled = !isLoading
        ) {
            Text("Sign in with Google", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "New here? Create an Account",
            color = Color(0xFFFF4081),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(top = 24.dp)
                .clickable { onNavigateToSignUp() }
        )
    }
}