package com.example.wepartyapp.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import android.content.Intent
import androidx.compose.material.icons.filled.Edit
import com.example.wepartyapp.ui.auth.LoginActivity

@Composable
fun ProfileScreenUI(
    onEditDietaryClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onEventDashboardClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    // 1. Use State variables so the UI knows to redraw when these change
    var userName by remember { mutableStateOf("Party Animal") }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }

    // 2. LaunchedEffect(Unit) forces this block to run every single time this screen is opened
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        userName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Party Animal"
        profilePhotoUri = user?.photoUrl
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
    ) {
        // --- TOP AVATAR SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFB65C5C)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Picture (Dynamically loads image OR shows placeholder)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoUri != null) {
                        AsyncImage(
                            model = profilePhotoUri,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Placeholder",
                            modifier = Modifier.size(60.dp),
                            tint = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- MENU OPTIONS SECTION ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            ProfileMenuRow(
                icon = Icons.Default.Settings,
                title = "Profile Settings",
                subtitle = "Update your name and profile picture",
                onClick = onEditProfileClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuRow(
                icon = Icons.Default.List,
                title = "Dietary Preferences",
                subtitle = "Manage your food allergies and preferences",
                onClick = onEditDietaryClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuRow(
                icon = Icons.Default.Edit,
                title = "Event Dashboard",
                subtitle = "Claim Items, Chat with event attendees, and view Location",
                onClick = onEventDashboardClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuRow(
                icon = Icons.Default.Close,
                title = "Log out",
                subtitle = "Sign out of your account",
                onClick = {
                    auth.signOut()

                    val intent = Intent(context, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }

                    context.startActivity(intent)
                }
            )
        }
    }
}

// --- Profile Settings ---
@Composable
fun ProfileSettingsScreenUI(onBack: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // State for the text field, image, and loading status
    var nickname by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(currentUser?.photoUrl) }
    var isUploading by remember { mutableStateOf(false) }

    // Launcher to open the phone's photo gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // Helper function to update the Auth profile once we have the final image URI
    val saveProfileData = { finalPhotoUri: Uri? ->
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(nickname)

        if (finalPhotoUri != null) {
            profileUpdates.setPhotoUri(finalPhotoUri)
        }

        currentUser?.updateProfile(profileUpdates.build())?.addOnCompleteListener { task ->
            isUploading = false
            if (task.isSuccessful) {
                Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                onBack()
            } else {
                Toast.makeText(context, "Update Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = { if (!isUploading) onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
        }

        Text("Edit Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB65C5C))
        Spacer(modifier = Modifier.height(32.dp))

        // Clickable Avatar for Upload
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, Color(0xFFB65C5C), CircleShape)
                .clickable(enabled = !isUploading) {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = "Upload", tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Text("Upload", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Nickname Input
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Display Nickname") },
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFB65C5C),
                focusedLabelColor = Color(0xFFB65C5C)
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = {
                if (isUploading) return@Button
                isUploading = true

                if (selectedImageUri != null && selectedImageUri.toString().startsWith("content://")) {

                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("profile_pictures/${currentUser?.uid}.jpg")

                    // 3. Using putStream securely bypasses Android 13+ gallery permissions so it doesn't crash
                    val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)

                    if (inputStream != null) {
                        storageRef.putStream(inputStream)
                            .addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                    saveProfileData(downloadUri)
                                }
                            }
                            .addOnFailureListener { e ->
                                isUploading = false
                                Toast.makeText(context, "Upload Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        isUploading = false
                        Toast.makeText(context, "Could not read the selected image.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    saveProfileData(selectedImageUri)
                }
            },
            enabled = !isUploading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Reusable component
@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE57373))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}