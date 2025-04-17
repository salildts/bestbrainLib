package com.app.bestbrain.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.bestbrain.R
import com.app.bestbrain.databinding.BbThreadHeaderItemBinding
import com.app.bestbrain.models.SessionGroup
import com.app.bestbrain.models.SessionItem

class BBThreadHeaderAdapter(
    val context: Context,
    val onThreadSelect: (SessionItem) -> Unit,
    val onDeleteClick: (String) -> Unit
) :
    RecyclerView.Adapter<BBThreadHeaderAdapter.ViewHolder>() {

    private var _sessionGroupList: List<SessionGroup> = arrayListOf()

    var sessionGroupList: List<SessionGroup>
        get() = _sessionGroupList
        set(value) {
            _sessionGroupList = value
            notifyDataSetChanged()
        }

    class ViewHolder(val binding: BbThreadHeaderItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            BbThreadHeaderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.binding.tvDate.text = _sessionGroupList[position].date

        val dividerItemDecoration = DividerItemDecoration(
            holder.binding.rvSession.context,
            (holder.binding.rvSession.layoutManager as LinearLayoutManager).orientation
        )
        dividerItemDecoration.setDrawable(
            ContextCompat.getDrawable(context, R.drawable.bb_white_line_divider)!!
        )
        holder.binding.rvSession.addItemDecoration(dividerItemDecoration)

        holder.binding.rvSession.adapter = BBThreadListAdapter(
            _sessionGroupList[position].sessions,
            onThreadSelect,
            onDeleteClick
        )
    }

    override fun getItemCount(): Int {
        return sessionGroupList.size
    }
}