package com.example.carteiravirtual

object WalletRepository {
    private val _balances = mutableMapOf(
        "BRL" to 100_000.0,
        "USD" to 50_000.0,
        "BTC" to 0.5000
    )
    val balances: Map<String, Double> get() = _balances

    fun add(currency: String, amount: Double) {
        _balances[currency] = (_balances[currency] ?: 0.0) + amount
    }
}