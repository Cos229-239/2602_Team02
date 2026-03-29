package com.example.wepartyapp.ui.api

data class SearchMetadata(
    val created_at: String,
    val id: String,
    val json_endpoint: String,
    val prettify_html_file: String,
    val processed_at: String,
    val raw_html_file: String,
    val status: String,
    val total_time_taken: String,
    val walmart_url: String
)