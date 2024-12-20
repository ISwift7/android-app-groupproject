package com.example.act22.data.model

data class User (
    var ID: Int,
    var name : String,
    var email : String,
    val portfolio : Portfolio,
    val balance : Double,
    val plan : String
)