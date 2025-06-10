package com.example.carteiravirtual

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carteiravirtual.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val balanceAdapter = BalanceAdapter()
    private val txAdapter = TransactionAdapter()

    private val transactions = mutableListOf(
        Transaction("BRL ==> BTC:       +2,2 BTC"),
        Transaction("BRL ==> USD:       +1.000 USD")
    )

    private fun currentWallet(): List<Balance> =
        WalletRepository.balances
            .map { (code, amount) -> Balance(code, amount) }
            .sortedBy { it.code }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupBalances()
        setupTransactions()
        setupListeners()
    }

    private fun setupBalances() = with(b.rvBalances) {
        layoutManager = GridLayoutManager(context, 3)
        adapter       = balanceAdapter
        refreshBalances()
    }

    private fun setupTransactions() {
        b.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = txAdapter
            isVerticalScrollBarEnabled = true
        }
        refreshTxList()
    }

    private fun setupListeners() {
        b.btnConvert.setOnClickListener {
            startActivity(Intent(this, ConvertActivity::class.java))
        }
    }

    private fun addTransaction(desc: String) {
        transactions.add(0, Transaction(desc))
        refreshTxList()
    }

    private fun refreshTxList() {
        txAdapter.submitList(transactions.toList())
    }

    override fun onResume() {
        super.onResume()
        refreshBalances()
    }

    private fun refreshBalances() {
        balanceAdapter.submit(currentWallet())
    }
}
