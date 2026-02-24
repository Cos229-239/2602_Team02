package com.example.wepartyapp.ui.api

data class WalmartPriceModel(
    val organic_results: List<OrganicResult>,
    val search_information: SearchInformation,
    val search_metadata: SearchMetadata,
    val search_parameters: SearchParameters
)