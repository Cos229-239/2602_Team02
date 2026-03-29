package com.example.wepartyapp.ui.create_event

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.utils.BaseActivity

class InviteFriendsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Grab the ID from the intent
        val existingEventId = intent.getStringExtra("EVENT_ID")

        setContent {
            val eventViewModel: EventViewModel by viewModels()

            // This calls your UI code from the InviteFriendsFragment.kt file
            InviteFriendsScreenUI(
                viewItemModel = eventViewModel,
                existingEventId = existingEventId
            )
        }
    }
}