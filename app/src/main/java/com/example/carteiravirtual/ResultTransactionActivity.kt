package com.example.carteiravirtual

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class ResultTransactionActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var textProgressBar: TextView
    private lateinit var textView3: TextView
    private lateinit var textView5: TextView
    private lateinit var textView7: TextView
    private lateinit var origem : String
    private lateinit var destino: String
    private var valorDigitado = 0.0
    private lateinit var servicoApi: ServicoApiMoeda
    private lateinit var rootLayout: ConstraintLayout
    private var taxaCambioAtual = 0.0
    private val formatoDecimal = DecimalFormat("#,##0.00")
    private val formatoBtc     = DecimalFormat("#,##0.0000")

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

        origem      = intent.getStringExtra(ConversionContract.EXTRA_ORIGIN) ?: ""
        destino     = intent.getStringExtra(ConversionContract.EXTRA_DEST)   ?: ""
        valorDigitado = intent.getDoubleExtra(ConversionContract.EXTRA_AMOUNT, 0.0)

        if (origem.isEmpty() || destino.isEmpty() || valorDigitado == 0.0) {
            Toast.makeText(this, "Dados inválidos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarRetrofit()
        showProgressBar()
        buscarTaxaCambio()
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

    private fun buscarTaxaCambio() {
        val par = obterParMoedas(origem, destino)

        servicoApi.obterTaxaCambio(par).enqueue(object : Callback<Map<String,TaxaCambio>> {
            override fun onResponse(call: Call<Map<String, TaxaCambio>>,
                                    resp: Response<Map<String, TaxaCambio>>
            ) {
                if (resp.isSuccessful && resp.body()?.isNotEmpty() == true) {
                    val taxa = resp.body()!!.values.first().bid.toDouble()
                    aplicarConversaoSucesso(taxa)
                } else usarTaxasMock()
            }
            override fun onFailure(c: Call<Map<String,TaxaCambio>>, t: Throwable) {
                usarTaxasMock()
            }
        })
    }

    private fun obterParMoedas(de: String, para: String): String {
        return when {
            de == "BRL" && para == "USD" -> "USD-BRL"
            de == "USD" && para == "BRL" -> "USD-BRL"
            de == "BRL" && para == "BTC" -> "BTC-BRL"
            de == "BTC" && para == "BRL" -> "BTC-BRL"
            de == "USD" && para == "BTC" -> "BTC-USD"
            de == "BTC" && para == "USD" -> "BTC-USD"
            else -> "USD-BRL"
        }
    }

    private fun usarTaxasMock() {
        val parMoedas = obterParMoedas(origem, destino)

        taxaCambioAtual = when (parMoedas) {
            "USD-BRL" -> 5.43
            "BTC-BRL" -> 615476.0
            "BTC-USD" -> 113350.0
            else      -> 1.0
        }
        aplicarConversaoSucesso(taxaCambioAtual)
    }

    private fun aplicarConversaoSucesso(taxa: Double) {
        val valorConvertido = calcularConversao(valorDigitado, taxa)

        WalletRepository.add(origem,  -valorDigitado)
        WalletRepository.add(destino,  valorConvertido)
        hideProgressBar()
        textView3.text = getString(R.string.textScreenResult)
        textView5.text = "Seu novo saldo em $destino é " + formatarSaldo(destino)

        rootLayout.setOnClickListener { finish() }
    }

    private fun calcularConversao(valor: Double, taxa: Double): Double = when {
        origem=="BRL" && destino=="USD" -> valor / taxa
        origem=="USD" && destino=="BRL" -> valor * taxa
        origem=="BRL" && destino=="BTC" -> valor / taxa
        origem=="BTC" && destino=="BRL" -> valor * taxa
        origem=="USD" && destino=="BTC" -> valor / taxa
        origem=="BTC" && destino=="USD" -> valor * taxa
        else -> valor
    }

    private fun configurarRetrofit() {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://economia.awesomeapi.com.br/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        servicoApi = retrofit.create(ServicoApiMoeda::class.java)
    }

    private fun formatarSaldo(moeda: String): String {
        val valor = WalletRepository.balances[moeda] ?: 0.0
        val txt   = if (moeda == "BTC") formatoBtc.format(valor)
        else                 formatoDecimal.format(valor)
        return "$txt $moeda"
    }
}