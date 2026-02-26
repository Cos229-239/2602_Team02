package com.example.wepartyapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wepartyapp.ui.create_event.PartyItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

// 1. Create a blueprint for an event
data class PartyEvent(
    val name: String,
    val time: String,
    val address: String,
    val date: LocalDate?
)

class EventViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // 2. Holds a list of events
    private val _events = MutableLiveData<List<PartyEvent>>(emptyList())
    val events: LiveData<List<PartyEvent>> = _events

    // --- Lesly read new comments ---

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
    }

    private fun fetchEventsFromFirebase() {
        db.collection("events").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                return@addSnapshotListener
            }

            val eventList = mutableListOf<PartyEvent>()

            // 3. Loop through every event in the database
            for (document in snapshot.documents) {
                val name = document.getString("name") ?: "Unknown Event"
                val time = document.getString("time") ?: "TBD"
                val address = document.getString("address") ?: "TBD"
                val dateString = document.getString("date")

                var date: LocalDate? = null
                if (!dateString.isNullOrEmpty()) {
                    try {
                        date = LocalDate.parse(dateString)
                    } catch (e: Exception) {
                        // ignore bad formatting
                    }
                }

                // Add it to our temporary list
                eventList.add(PartyEvent(name, time, address, date))
            }

            // 4. Update the UI with the full list
            _events.value = eventList
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
            "items" to mappedItems // <-- Safely mapped and easy to pull down later
        )

        // 3. Push to Firebase
        db.collection("events").add(eventMap).addOnSuccessListener {
            // 4. Clear the cache so the next event starts completely fresh
            eventName = ""
            eventSummary = ""
            eventDate = ""
            eventTime = ""
            eventAddress = ""
            _itemsList.value = emptyList()
        }
    }
}