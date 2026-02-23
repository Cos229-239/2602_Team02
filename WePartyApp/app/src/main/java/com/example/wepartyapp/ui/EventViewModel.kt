package com.example.wepartyapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EventViewModel : ViewModel() {

    // These variables hold the data. We make them "Mutable" so we can change them.
    private val _eventName = MutableLiveData<String>()
    private val _eventTime = MutableLiveData<String>()
    private val _eventAddress = MutableLiveData<String>()

    // These are what the screens will READ.
    val eventName: LiveData<String> = _eventName
    val eventTime: LiveData<String> = _eventTime
    val eventAddress: LiveData<String> = _eventAddress

    // This block runs immediately when the app starts.
    // We are putting FAKE data here for now so you can see it working.
    // Later, we will delete this and let the "Create Event" screen fill it in.
    init {
        _eventName.value = "Valentines Day Party!!"
        _eventTime.value = "Feb 14, 2026 @ 8:00 PM"
        _eventAddress.value = "123 Mickey Ln, Winter Park, FL"
    }

    // Call this function later to update the data from the "Create Event" screen
    fun saveEventData(name: String, time: String, address: String) {
        _eventName.value = name
        _eventTime.value = time
        _eventAddress.value = address
    }
}