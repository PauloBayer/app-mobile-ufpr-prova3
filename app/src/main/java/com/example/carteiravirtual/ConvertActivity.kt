package com.example.carteiravirtual

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.carteiravirtual.databinding.ActivityConvertBinding

class ConvertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConvertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConvertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
    }
}
