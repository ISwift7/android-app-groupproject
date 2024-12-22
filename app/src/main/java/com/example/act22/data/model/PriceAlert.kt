package com.example.act22.data.model

data class PriceAlert(
    val id: String = "",
    val symbol: String,
    val is_crypto: Boolean,
    val target_price: Double,
    val alert_type: String, // 'above' or 'below'
    val current_price_at_creation: Double,
    val status: String = "active", // 'active' or 'triggered'
    val created_at: String = "",
    val triggered_at: String? = null,
    val price_at_trigger: Double? = null
)