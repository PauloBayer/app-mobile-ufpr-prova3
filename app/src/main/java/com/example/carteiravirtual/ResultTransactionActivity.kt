package com.example.carteiravirtual

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class ResultTransactionActivity : AppCompatActivity() {

    private lateinit var rootLayout     : ConstraintLayout
    private lateinit var progressBar    : ProgressBar
    private lateinit var tvProgress     : TextView
    private lateinit var tvSuccess      : TextView
    private lateinit var tvNewBalance   : TextView
    private lateinit var btnBack        : MaterialButton

    private lateinit var origem  : String
    private lateinit var destino : String
    private var     valorDigitado = 0.0

    private lateinit var api: ServicoApiMoeda

    // format helpers
    private val formatoDecimal = DecimalFormat("#,##0.00")
    private val formatoBtc     = DecimalFormat("#,##0.0000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_transaction)

        bindViews()
        readIntentOrFinish()
        initRetrofit()

        showLoader()
        animateBar()
        buscarTaxaCambio()
    }

    private fun bindViews() {
        rootLayout   = findViewById(R.id.result_transaction)
        progressBar  = findViewById(R.id.progressBar)
        tvProgress   = findViewById(R.id.textProgressBar)
        tvSuccess    = findViewById(R.id.tvSuccess)
        tvNewBalance = findViewById(R.id.tvNewBalance)
        btnBack      = findViewById(R.id.btnBack)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, ins ->
            val bars = ins.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            ins
        }
        btnBack.setOnClickListener { finish() }
    }

    private fun readIntentOrFinish() {
        origem        = intent.getStringExtra(ConversionContract.EXTRA_ORIGIN) ?: ""
        destino       = intent.getStringExtra(ConversionContract.EXTRA_DEST)   ?: ""
        valorDigitado = intent.getDoubleExtra(ConversionContract.EXTRA_AMOUNT, 0.0)

        if (origem.isEmpty() || destino.isEmpty() || valorDigitado == 0.0) {
            Toast.makeText(this, "Dados inválidos", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showLoader() {
        progressBar.visibility = View.VISIBLE
        tvProgress.visibility  = View.VISIBLE

        tvSuccess.visibility    = View.GONE
        tvNewBalance.visibility = View.GONE
        btnBack.visibility      = View.GONE
    }

    private fun hideLoader() {
        progressBar.visibility = View.GONE
        tvProgress.visibility  = View.GONE

        tvSuccess.visibility    = View.VISIBLE
        tvNewBalance.visibility = View.VISIBLE
        btnBack.visibility      = View.VISIBLE
    }

    private fun animateBar() {
        progressBar.progress = 0
        ObjectAnimator.ofInt(progressBar, "progress", 0, 100).apply {
            duration = 1500L
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun buscarTaxaCambio() {
        val par = obterParMoedas(origem, destino)

        api.obterTaxaCambio(par).enqueue(object : Callback<Map<String, TaxaCambio>> {
            override fun onResponse(
                call: Call<Map<String, TaxaCambio>>,
                response: Response<Map<String, TaxaCambio>>
            ) {
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val taxa = response.body()!!.values.first().bid.toDouble()
                    aplicarConversaoSucesso(taxa)
                } else {
                    Toast.makeText(
                        this@ResultTransactionActivity,
                        "Falha ao buscar taxa de câmbio",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
            override fun onFailure(call: Call<Map<String, TaxaCambio>>, t: Throwable) {
                Toast.makeText(
                    this@ResultTransactionActivity,
                    "Falha ao buscar taxa de câmbio",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        })
    }

    private fun aplicarConversaoSucesso(taxa: Double) {
        val valorConvertido = calcularConversao(valorDigitado, taxa)

        WalletRepository.add(origem,  -valorDigitado)
        WalletRepository.add(destino,  valorConvertido)

        rootLayout.postDelayed({
            tvSuccess.text    = getString(R.string.textScreenResult)
            tvNewBalance.text = "Seu novo saldo em $destino é ${formatarSaldo(destino)}"
            hideLoader()
        }, 3000)
    }

    private fun calcularConversao(v: Double, t: Double): Double = when {
        origem=="BRL" && destino=="USD" -> v / t
        origem=="USD" && destino=="BRL" -> v * t
        origem=="BRL" && destino=="BTC" -> v / t
        origem=="BTC" && destino=="BRL" -> v * t
        origem=="USD" && destino=="BTC" -> v / t
        origem=="BTC" && destino=="USD" -> v * t
        else -> v
    }

    private fun obterParMoedas(de: String, para: String) = when {
        de=="BRL" && para=="USD" -> "USD-BRL"
        de=="USD" && para=="BRL" -> "USD-BRL"
        de=="BRL" && para=="BTC" -> "BTC-BRL"
        de=="BTC" && para=="BRL" -> "BTC-BRL"
        de=="USD" && para=="BTC" -> "BTC-USD"
        de=="BTC" && para=="USD" -> "BTC-USD"
        else -> "USD-BRL"
    }

    private fun formatarSaldo(m: String): String {
        val valor = WalletRepository.balances[m] ?: 0.0
        val txt   = if (m=="BTC") formatoBtc.format(valor)
        else           formatoDecimal.format(valor)
        return "$txt $m"
    }

    private fun initRetrofit() {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        api = Retrofit.Builder()
            .baseUrl("https://economia.awesomeapi.com.br/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServicoApiMoeda::class.java)
    }
}
