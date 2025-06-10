package com.example.carteiravirtual

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carteiravirtual.databinding.ItemBalanceBinding

data class Balance(val code: String, val amount: Double)

class BalanceAdapter(
    private var items: List<Balance> = emptyList()
) : RecyclerView.Adapter<BalanceAdapter.VH>() {

    fun submit(list: List<Balance>) {
        items = list
        notifyDataSetChanged()
    }

    inner class VH(private val b: ItemBalanceBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(balance: Balance) = with(b) {
            tvCurrency.text = balance.code
            tvAmount.text =
                if (balance.code == "BTC") String.format("%.4f", balance.amount)
                else String.format("%,.2f", balance.amount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBalanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}
