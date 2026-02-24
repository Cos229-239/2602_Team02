package com.example.wepartyapp.ui.api

data class SearchInformation(
    val location: Location,
    val organic_results_state: String,
    val query_displayed: String,
    val time_taken: String,
    val total_results: String
)