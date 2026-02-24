package com.example.wepartyapp.ui.api

data class OrganicResult(
    val free_shipping: String,
    val free_shipping_with_walmart_plus: String,
    val muliple_options_available: String,
    val multiple_options_available: String,
    val out_of_stock: String,
    val price_per_unit: PricePerUnit,
    val primary_offer: PrimaryOffer,
    val product_id: String,
    val product_page_url: String,
    val rating: String,
    val reviews: String,
    val seller_id: String,
    val seller_name: String,
    val serpapi_product_page_url: String,
    val sponsored: Boolean,
    val thumbnail: String,
    val title: String,
    val two_day_shipping: String,
    val us_item_id: String
)
