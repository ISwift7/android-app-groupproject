package com.example.act22.data.model

enum class AssetType {
    CRYPTO, STOCK, ALL
}

// Superclass Asset with Firebase serialization support
sealed class Asset(
    open val ID: String,
    open val name: String,
    open var price: Double,
    open val iconUrl: String
) {
    // Required empty constructor for Firebase
    constructor() : this("", "", 0.0, "")
}

// TechStock subclass with Firebase serialization
data class TechStock(
    override val ID: String = "",
    override val name: String = "",
    override var price: Double = 0.0,
    override val iconUrl: String = "",
    val sector: String = "",
    val high: Double = 0.0,
    val low: Double = 0.0,
    val open: Double = 0.0,
    val previous_close: Double = 0.0,
    val timestamp: String = "",
    val country: String = ""
) : Asset(ID, name, price, iconUrl)

// Crypto subclass with Firebase serialization
data class Crypto(
    override val ID: String = "",
    override val name: String = "",
    override var price: Double = 0.0,
    override val iconUrl: String = "",
    val blockchain: String = "",
    val high: Double = 0.0,
    val low: Double = 0.0,
    val previous_close: Double = 0.0,
    val timestamp: String = ""
) : Asset(ID, name, price, iconUrl)


