package com.example.carteiravirtual

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.carteiravirtual.databinding.ItemTransactionBinding

class TransactionAdapter :
    ListAdapter<Transaction, TransactionAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemTransactionBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(tx: Transaction) = with(b) {
            tvLine.text = tx.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(o1: Transaction, o2: Transaction) = o1.timestamp == o2.timestamp
            override fun areContentsTheSame(o1: Transaction, o2: Transaction) = o1 == o2
        }
    }
}

