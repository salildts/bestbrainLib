package com.app.bestbrain.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.bestbrain.R
import com.app.bestbrain.databinding.BbChatButtonItemBinding
import com.app.bestbrain.models.ChatMessageModel

class ChatButtonAdapter(
    private val context: Context,
    private val bb_buttons: List<ChatMessageModel.Data.BbButton?>?,
    val onButtonClick: (String) -> Unit
) : RecyclerView.Adapter<ChatButtonAdapter.ViewHolder>() {

    class ViewHolder(val binding: BbChatButtonItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            BbChatButtonItemBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.btnMsg.text = bb_buttons?.get(position)?.label ?: ""

        holder.binding.btnMsg.setOnClickListener {
            onButtonClick(bb_buttons?.get(position)?.value ?: "")
            if (bb_buttons != null) {
                for (button in bb_buttons) {
                    button!!.enable = false
                }
            }
        }

        holder.binding.btnMsg.isEnabled = bb_buttons?.get(position)?.enable == true
        if (bb_buttons?.get(position)?.enable == true) {
            holder.binding.btnMsg.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.app_btn
                )
            )
            holder.binding.btnMsg.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.btn_text_color
                )
            )
        } else {
            holder.binding.btnMsg.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.bb_gray_btn_bg
                )
            )
            holder.binding.btnMsg.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }

    override fun getItemCount(): Int {
        return bb_buttons?.size ?: 0
    }
}