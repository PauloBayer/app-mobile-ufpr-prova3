package com.example.carteiravirtual

data class Transaction(
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)