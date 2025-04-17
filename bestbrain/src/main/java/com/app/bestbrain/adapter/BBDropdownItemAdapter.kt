package com.app.bestbrain.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.bestbrain.databinding.BbDropdownItemBinding
import com.app.bestbrain.models.BbOption

class BBDropdownItemAdapter(
    val itemList: ArrayList<BbOption>,
    val onItemSelect: (BbOption) -> Unit
) :
    RecyclerView.Adapter<BBDropdownItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: BbDropdownItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            BbDropdownItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.binding.tvItemName.text = itemList[position].label ?: ""

        holder.itemView.setOnClickListener {
            onItemSelect(itemList[position])
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}