package com.example.wepartyapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wepartyapp.ui.create_event.PartyItem
import com.example.wepartyapp.ui.event_dashboard.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// The blueprint for our real notifications
data class PartyNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val timestamp: Long // Used to sort them from newest to oldest!
)

// 1. Create a blueprint for an event
data class PartyEvent(
    val id: String = "", // Added ID to uniquely identify events for chat
    val name: String,
    val time: String,
    val address: String,
    val date: LocalDate?,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val lastSenderId: String? = null,
    val eventItems: List<EventItems> = emptyList()
)

data class EventItems(                  //data class of the map structure within the firestore array
    val name: String,
    val price: String
)

class EventViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 2. Holds a list of events
    private val _events = MutableLiveData<List<PartyEvent>>(emptyList())
    val events: LiveData<List<PartyEvent>> = _events

    // --- Notification State ---
    private val _notificationsList = MutableStateFlow<List<PartyNotification>>(emptyList())
    val notificationsList: StateFlow<List<PartyNotification>> = _notificationsList.asStateFlow()

    // --- Chat State ---
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // We cache the text fields here so they survive navigation between screens
    var eventName by mutableStateOf("")
    var eventSummary by mutableStateOf("")
    var eventDate by mutableStateOf("")
    var eventTime by mutableStateOf("")
    var eventAddress by mutableStateOf("")

    //-list of PartyItems-
    private val _itemsList = MutableStateFlow<List<PartyItem>>(emptyList())
    val _items: StateFlow<List<PartyItem>> = _itemsList.asStateFlow()

    //-adds a PartyItem to our list-
    fun addItems(item: PartyItem) {
        _itemsList.update { currentList -> currentList + item }
    }
    //-updates the price of a PartyItem in the list-
    fun updatePrice(itemName: String, updatedPrice: String) {
        _itemsList.update { currentList ->
            currentList.map { item ->
                if(item.name == itemName) {
                    item.copy(price = updatedPrice)
                } else {
                    item
                }
            }
        }
    }

    init {
        fetchEventsFromFirebase()
        fetchNotificationsFromFirebase() // <-- Starts listening for alerts immediately
    }

    private fun fetchEventsFromFirebase() {
        db.collection("events").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                return@addSnapshotListener
            }

            val eventList = mutableListOf<PartyEvent>()

            // 3. Loop through every event in the database
            for (document in snapshot.documents) {
                val id = document.id
                val name = document.getString("name") ?: "Unknown Event"
                val time = document.getString("time") ?: "TBD"
                val address = document.getString("address") ?: "TBD"
                val dateString = document.getString("date")
                val lastMsg = document.getString("lastMessage")
                val lastMsgTime = document.getLong("lastMessageTime")
                val lastSender = document.getString("lastSenderId")

                var date: LocalDate? = null
                if (!dateString.isNullOrEmpty()) {
                    try {
                        date = LocalDate.parse(dateString)
                    } catch (e: Exception) {
                        // ignore bad formatting
                    }
                }

                val arrayOfItems = document.get("items") as? List<Map<String, String>>      //getting the array of maps
                val eventItems = arrayOfItems?.map { map ->                                 //List<Map> to List<EventItems>
                    EventItems(
                        name = map["name"] as? String ?: "",
                        price = map["price"] as? String ?: ""
                    )
                } ?: emptyList()

                // Add it to our temporary list
                eventList.add(PartyEvent(id, name, time, address, date, lastMsg, lastMsgTime, lastSender, eventItems))
            }

            // 4. Update the UI with the full list
            _events.value = eventList
        }
    }

    private fun fetchNotificationsFromFirebase() {
        // Listens to the "notifications" collection and sorts by newest first
        db.collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val alerts = mutableListOf<PartyNotification>()
                for (document in snapshot.documents) {
                    val id = document.id
                    val title = document.getString("title") ?: "Alert"
                    val message = document.getString("message") ?: ""
                    val time = document.getString("time") ?: "Just now"
                    val timestamp = document.getLong("timestamp") ?: 0L

                    alerts.add(PartyNotification(id, title, message, time, timestamp))
                }
                _notificationsList.value = alerts
            }
    }

    // --- Chat Functions ---

    fun listenToMessages(eventId: String) {
        db.collection("events").document(eventId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val msgs = snapshot.documents.map { doc ->
                    ChatMessage(
                        id = doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "Anonymous",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                _messages.value = msgs
            }
    }

    fun sendMessage(eventId: String, text: String) {
        val user = auth.currentUser ?: return
        val messageData = hashMapOf(
            "senderId" to user.uid,
            "senderName" to (user.displayName ?: "User"),
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("events").document(eventId).collection("messages").add(messageData)
            .addOnSuccessListener {
                // Update last message in the event document for the inbox snippet
                db.collection("events").document(eventId).update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTime" to messageData["timestamp"],
                        "lastSenderId" to user.uid
                    )
                )
            }
    }

    // Firebase push function (No parameters needed anymore, it pulls directly from the ViewModel cache)
    fun saveEventData() {
        // 1. Convert our custom PartyItem list into a simple map so Firebase doesn't crash
        val mappedItems = _itemsList.value.map {
            mapOf("name" to it.name, "price" to it.price)
        }

        // 2. Package everything up into one clean map
        val eventMap = hashMapOf(
            "name" to eventName,
            "summary" to eventSummary,
            "time" to eventTime,
            "address" to eventAddress,
            "date" to eventDate,
            "items" to mappedItems, // <-- Safely mapped and easy to pull down later
            "lastMessage" to null,
            "lastMessageTime" to null,
            "lastSenderId" to null
        )

        // 3. Push to Firebase
        db.collection("events").add(eventMap).addOnSuccessListener {

            // --- Format Date for Notifications ---
            var displayDate = eventDate
            try {
                if (eventDate.isNotBlank()) {
                    val parsedDate = LocalDate.parse(eventDate)
                    val formatter = DateTimeFormatter.ofPattern("MMM. d, yyyy")
                    displayDate = parsedDate.format(formatter)
                }
            } catch (e: Exception) {
                // If it fails for any reason, it just falls back to the original text
            }

            // --- Send an alert to the notifications feed ---
            sendAppNotification(
                title = "New Party Alert!",
                message = "$eventName is happening on $displayDate. Tap to see details!"
            )

            // 4. Clear the cache so the next event starts completely fresh
            eventName = ""
            eventSummary = ""
            eventDate = ""
            eventTime = ""
            eventAddress = ""
            _itemsList.value = emptyList()
        }
    }

    // --- Creates an Alert in the Notifications Database ---
    private fun sendAppNotification(title: String, message: String) {
        val notificationMap = hashMapOf(
            "title" to title,
            "message" to message,
            "time" to "Just now",
            "timestamp" to System.currentTimeMillis() // This ensures the newest alerts stay at the top
        )

        db.collection("notifications").add(notificationMap)
    }

    // --- Deletes an Event From Firebase ---
    fun deleteEvent(event: PartyEvent) {
        // Because the date is stored as a String in Firebase (yyyy-MM-dd), we convert it back
        val dateString = event.date?.toString() ?: ""

        // Find the exact event matching the name, time, and date, then destroy it
        db.collection("events")
            .whereEqualTo("name", event.name)
            .whereEqualTo("time", event.time)
            .whereEqualTo("date", dateString)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
            }
    }
}