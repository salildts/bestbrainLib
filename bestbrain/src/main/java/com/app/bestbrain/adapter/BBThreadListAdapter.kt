package com.app.bestbrain.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.bestbrain.databinding.BbThreadListItemBinding
import com.app.bestbrain.models.SessionItem

class BBThreadListAdapter(
    val threadList: List<SessionItem>,
    val onThreadSelect: (SessionItem) -> Unit,
    val onDeleteClick: (String) -> Unit
) :
    RecyclerView.Adapter<BBThreadListAdapter.ViewHolder>() {

    class ViewHolder(val binding: BbThreadListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            BbThreadListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.binding.tvThreadName.text = threadList[position].session_title

        holder.itemView.setOnClickListener {
            onThreadSelect(threadList[position])
        }

        holder.binding.btnDelete.setOnClickListener {
            onDeleteClick(threadList[position].session_id)
        }
    }

    override fun getItemCount(): Int {
        return threadList.size
    }
}