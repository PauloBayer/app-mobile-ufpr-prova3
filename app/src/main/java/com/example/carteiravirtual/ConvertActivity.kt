package com.example.carteiravirtual

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import android.content.Intent

class ConvertActivity : AppCompatActivity() {

    private lateinit var setValorConverter: EditText
    private lateinit var setValorConvertido: EditText
    private lateinit var tvInfoConversao: TextView
    private lateinit var btnConverter: MaterialButton
    private lateinit var barraProgresso: ProgressBar
    
    private lateinit var tvSaldoBrl: TextView
    private lateinit var tvSaldoUsd: TextView
    private lateinit var tvSaldoBtc: TextView
    
    private lateinit var cardBrlOrigem: CardView
    private lateinit var cardUsdOrigem: CardView
    private lateinit var cardBtcOrigem: CardView
    
    private lateinit var cardBrlDestino: CardView
    private lateinit var cardUsdDestino: CardView
    private lateinit var cardBtcDestino: CardView
    
    private var moedaOrigemSelecionada: String? = null
    private var moedaDestinoSelecionada: String? = null
    private var taxaCambioAtual: Double = 0.0
    
    private fun obterSaldoBRL(): Double = WalletRepository.balances["BRL"] ?: 100000.00
    private fun obterSaldoUSD(): Double = WalletRepository.balances["USD"] ?: 50000.00
    private fun obterSaldoBTC(): Double = WalletRepository.balances["BTC"] ?: 0.5000
    
    private val formatoDecimal = DecimalFormat("#,##0.00")
    private val formatoBtc = DecimalFormat("#,##0.0000")
    
    private lateinit var servicoApi: ServicoApiMoeda
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert)
        
        inicializarViews()
        configurarRetrofit()
        configurarListeners()
        atualizarSaldos()
    }
    
    private fun inicializarViews() {
        setValorConverter = findViewById(R.id.et_valor_converter)
        setValorConvertido = findViewById(R.id.et_valor_convertido)
        tvInfoConversao = findViewById(R.id.tv_conversion_info)
        btnConverter = findViewById(R.id.btn_converter)
        barraProgresso = findViewById(R.id.progress_bar)
        
        tvSaldoBrl = findViewById(R.id.tv_brl_balance)
        tvSaldoUsd = findViewById(R.id.tv_usd_balance)
        tvSaldoBtc = findViewById(R.id.tv_btc_balance)
        
        cardBrlOrigem = findViewById(R.id.card_brl_origem)
        cardUsdOrigem = findViewById(R.id.card_usd_origem)
        cardBtcOrigem = findViewById(R.id.card_btc_origem)
        
        cardBrlDestino = findViewById(R.id.card_brl_destino)
        cardUsdDestino = findViewById(R.id.card_usd_destino)
        cardBtcDestino = findViewById(R.id.card_btc_destino)
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
    
    private fun configurarListeners() {
        cardBrlOrigem.setOnClickListener { selecionarMoedaOrigem("BRL") }
        cardUsdOrigem.setOnClickListener { selecionarMoedaOrigem("USD") }
        cardBtcOrigem.setOnClickListener { selecionarMoedaOrigem("BTC") }
        
        cardBrlDestino.setOnClickListener { selecionarMoedaDestino("BRL") }
        cardUsdDestino.setOnClickListener { selecionarMoedaDestino("USD") }
        cardBtcDestino.setOnClickListener { selecionarMoedaDestino("BTC") }
        
        setValorConverter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                atualizarConversao()
            }
        })
        
        btnConverter.setOnClickListener {

            realizarConversao()
        }
    }
    
    private fun selecionarMoedaOrigem(moeda: String) {
        moedaOrigemSelecionada = moeda
        atualizarSelecaoCards()
        buscarTaxaCambio()
    }
    
    private fun selecionarMoedaDestino(moeda: String) {
        moedaDestinoSelecionada = moeda
        atualizarSelecaoCards()
        buscarTaxaCambio()
    }
    
    private fun atualizarSelecaoCards() {
        resetarCoresCards()
        
        when (moedaOrigemSelecionada) {
            "BRL" -> {
                cardBrlOrigem.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected))
                cardBrlOrigem.cardElevation = 16f
                cardBrlOrigem.alpha = 1.0f
            }
            "USD" -> {
                cardUsdOrigem.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected))
                cardUsdOrigem.cardElevation = 16f
                cardUsdOrigem.alpha = 1.0f
            }
            "BTC" -> {
                cardBtcOrigem.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected))
                cardBtcOrigem.cardElevation = 16f
                cardBtcOrigem.alpha = 1.0f
            }
        }
        
        when (moedaDestinoSelecionada) {
            "BRL" -> {
                cardBrlDestino.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected))
                cardBrlDestino.cardElevation = 16f
                cardBrlDestino.alpha = 1.0f
            }
            "USD" -> {
                cardUsdDestino.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected))
                cardUsdDestino.cardElevation = 16f
                cardUsdDestino.alpha = 1.0f
            }
            "BTC" -> {
                cardBtcDestino.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected))
                cardBtcDestino.cardElevation = 16f
                cardBtcDestino.alpha = 1.0f
            }
        }
        
        atualizarInfoConversao()
    }
    
    private fun resetarCoresCards() {
        val corCard = ContextCompat.getColor(this, R.color.card_background)
        val elevacaoNormal = 8f
        val alphaNormal = 0.8f
        
        cardBrlOrigem.setCardBackgroundColor(corCard)
        cardBrlOrigem.cardElevation = elevacaoNormal
        cardBrlOrigem.alpha = alphaNormal
        
        cardUsdOrigem.setCardBackgroundColor(corCard)
        cardUsdOrigem.cardElevation = elevacaoNormal
        cardUsdOrigem.alpha = alphaNormal
        
        cardBtcOrigem.setCardBackgroundColor(corCard)
        cardBtcOrigem.cardElevation = elevacaoNormal
        cardBtcOrigem.alpha = alphaNormal
        
        cardBrlDestino.setCardBackgroundColor(corCard)
        cardBrlDestino.cardElevation = elevacaoNormal
        cardBrlDestino.alpha = alphaNormal
        
        cardUsdDestino.setCardBackgroundColor(corCard)
        cardUsdDestino.cardElevation = elevacaoNormal
        cardUsdDestino.alpha = alphaNormal
        
        cardBtcDestino.setCardBackgroundColor(corCard)
        cardBtcDestino.cardElevation = elevacaoNormal
        cardBtcDestino.alpha = alphaNormal
    }
    
    private fun atualizarInfoConversao() {
        if (moedaOrigemSelecionada != null && moedaDestinoSelecionada != null) {
            if (moedaOrigemSelecionada == moedaDestinoSelecionada) {
                tvInfoConversao.text = "Selecione moedas diferentes"
                btnConverter.isEnabled = false
                return
            }
            
            val info = "1 $moedaOrigemSelecionada = ${formatarTaxa(taxaCambioAtual)} $moedaDestinoSelecionada"
            tvInfoConversao.text = info
            btnConverter.isEnabled = setValorConverter.text.toString().isNotEmpty()
        } else {
            tvInfoConversao.text = "Selecione moedas para conversão"
            btnConverter.isEnabled = false
        }
    }
    
    private fun formatarTaxa(taxa: Double): String {
        return if (moedaDestinoSelecionada == "BTC") {
            formatoBtc.format(taxa)
        } else {
            formatoDecimal.format(taxa)
        }
    }
    
    private fun buscarTaxaCambio() {
        if (moedaOrigemSelecionada == null || moedaDestinoSelecionada == null) return
        if (moedaOrigemSelecionada == moedaDestinoSelecionada) return
        
        val parMoedas = obterParMoedas(moedaOrigemSelecionada!!, moedaDestinoSelecionada!!)
        
        barraProgresso.visibility = View.VISIBLE
        
        // Primeiro tenta a API real
        servicoApi.obterTaxaCambio(parMoedas).enqueue(object : Callback<Map<String, TaxaCambio>> {
            override fun onResponse(call: Call<Map<String, TaxaCambio>>, response: Response<Map<String, TaxaCambio>>) {
                barraProgresso.visibility = View.GONE
                
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val taxaData = response.body()!!.values.first()
                    val taxa = taxaData.bid.toDouble()
                    taxaCambioAtual = taxa
                    atualizarInfoConversao()
                    atualizarConversao()
                } else {
                    usarTaxasMock()
                }
            }
            
            override fun onFailure(call: Call<Map<String, TaxaCambio>>, t: Throwable) {
                barraProgresso.visibility = View.GONE
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
    
    private fun atualizarConversao() {
        val textoEntrada = setValorConverter.text.toString()
        if (textoEntrada.isEmpty() || taxaCambioAtual == 0.0) {
            setValorConvertido.setText("")
            return
        }
        
        try {
            val valorEntrada = textoEntrada.toDouble()
            val valorConvertido = calcularConversao(valorEntrada)
            
            val valorFormatado = if (moedaDestinoSelecionada == "BTC") {
                formatoBtc.format(valorConvertido)
            } else {
                formatoDecimal.format(valorConvertido)
            }
            
            setValorConvertido.setText(valorFormatado)
        } catch (e: NumberFormatException) {
            setValorConvertido.setText("")
        }
    }
    
    private fun calcularConversao(valor: Double): Double {
        return when {
            moedaOrigemSelecionada == "BRL" && moedaDestinoSelecionada == "USD" -> valor / taxaCambioAtual
            moedaOrigemSelecionada == "USD" && moedaDestinoSelecionada == "BRL" -> valor * taxaCambioAtual
            moedaOrigemSelecionada == "BRL" && moedaDestinoSelecionada == "BTC" -> valor / taxaCambioAtual
            moedaOrigemSelecionada == "BTC" && moedaDestinoSelecionada == "BRL" -> valor * taxaCambioAtual
            moedaOrigemSelecionada == "USD" && moedaDestinoSelecionada == "BTC" -> valor / taxaCambioAtual
            moedaOrigemSelecionada == "BTC" && moedaDestinoSelecionada == "USD" -> valor * taxaCambioAtual
            else -> valor
        }
    }

    private fun realizarConversao() {
        if (moedaOrigemSelecionada == null || moedaDestinoSelecionada == null) {
            Toast.makeText(this, "Escolha moeda de origem e destino", Toast.LENGTH_SHORT).show()
            return
        }
        if (moedaOrigemSelecionada == moedaDestinoSelecionada) {
            Toast.makeText(this, "Selecione moedas diferentes", Toast.LENGTH_SHORT).show()
            return
        }

        val textoEntrada = setValorConverter.text.toString()
        val valorEntrada = textoEntrada.toDoubleOrNull()
        if (valorEntrada == null || valorEntrada <= 0) {
            Toast.makeText(this, "Digite um valor válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (!temSaldoSuficiente(valorEntrada)) {
            Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
            return
        }

        val i = Intent(this, ResultTransactionActivity::class.java).apply {
            putExtra(ConversionContract.EXTRA_ORIGIN,  moedaOrigemSelecionada)
            putExtra(ConversionContract.EXTRA_DEST,    moedaDestinoSelecionada)
            putExtra(ConversionContract.EXTRA_AMOUNT,  valorEntrada)
        }
        startActivity(i)

        setValorConverter.text?.clear()
        setValorConvertido.text?.clear()
        btnConverter.isEnabled = false
        tvInfoConversao.text = "Selecione moedas para conversão"
    }
    
    private fun temSaldoSuficiente(valor: Double): Boolean {
        return when (moedaOrigemSelecionada) {
            "BRL" -> obterSaldoBRL() >= valor
            "USD" -> obterSaldoUSD() >= valor
            "BTC" -> obterSaldoBTC() >= valor
            else -> false
        }
    }
    
    private fun atualizarSaldosAposConversao(valorOriginal: Double, valorConvertido: Double) {
        when (moedaOrigemSelecionada) {
            "BRL" -> WalletRepository.add("BRL", -valorOriginal)
            "USD" -> WalletRepository.add("USD", -valorOriginal)
            "BTC" -> WalletRepository.add("BTC", -valorOriginal)
        }
        
        when (moedaDestinoSelecionada) {
            "BRL" -> WalletRepository.add("BRL", valorConvertido)
            "USD" -> WalletRepository.add("USD", valorConvertido)
            "BTC" -> WalletRepository.add("BTC", valorConvertido)
        }
        
        atualizarSaldos()
    }
    
    private fun atualizarSaldos() {
        tvSaldoBrl.text = formatoDecimal.format(obterSaldoBRL())
        tvSaldoUsd.text = formatoDecimal.format(obterSaldoUSD())
        tvSaldoBtc.text = formatoBtc.format(obterSaldoBTC())
    }
    
    private fun formatarValor(valor: Double, moeda: String): String {
        val formatado = if (moeda == "BTC") {
            formatoBtc.format(valor)
        } else {
            formatoDecimal.format(valor)
        }
        return "$formatado $moeda"
    }
    
    private fun usarTaxasMock() {
        val parMoedas = obterParMoedas(moedaOrigemSelecionada!!, moedaDestinoSelecionada!!)
        
        taxaCambioAtual = when (parMoedas) {
            "USD-BRL" -> 5.43
            "BTC-BRL" -> 615476.0
            "BTC-USD" -> 113350.0
            else -> 1.0
        }
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            atualizarInfoConversao()
            atualizarConversao()
        }, 500)
    }
}

interface ServicoApiMoeda {
    @GET("last/{currency}")
    fun obterTaxaCambio(@Path("currency") currency: String): Call<Map<String, TaxaCambio>>
}

data class TaxaCambio(
    val code: String,
    val codein: String,
    val name: String,
    val high: String,
    val low: String,
    val varBid: String,
    val pctChange: String,
    val bid: String,
    val ask: String,
    val timestamp: String,
    val create_date: String
)
