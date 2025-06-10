package com.example.carteiravirtual

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ResultTransactionActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var textProgressBar: TextView
    private lateinit var textView3: TextView
    private lateinit var textView5: TextView
    private lateinit var textView7: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_transaction)
        val rootLayout = findViewById<ConstraintLayout>(R.id.result_transaction)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBar = findViewById(R.id.progressBar)
        textProgressBar = findViewById(R.id.textProgressBar)
        textView3 = findViewById(R.id.textView3)
        textView5 = findViewById(R.id.textView5)
        textView7 = findViewById(R.id.textView7)

        showProgressBar()
        simulateLoading()
    }
    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        textProgressBar.visibility = View.VISIBLE
        textView3.visibility = View.INVISIBLE
        textView5.visibility = View.INVISIBLE
        textView7.visibility = View.INVISIBLE
        progressBar.progress = 0
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
        textProgressBar.visibility = View.INVISIBLE
        textView3.visibility = View.VISIBLE
        textView5.visibility = View.VISIBLE
        textView7.visibility = View.VISIBLE


    }

    private fun simulateLoading() {
        Thread {
            for (i in 1..100) {
                Thread.sleep(30)
                runOnUiThread {
                    progressBar.progress = i
                }
            }
            runOnUiThread {
                hideProgressBar()
            }
        }.start()
    }
}