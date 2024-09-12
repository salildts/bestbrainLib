package com.app.bestbrain.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.bestbrain.databinding.ChatReceiveItemButtonBinding
import com.app.bestbrain.databinding.ChatReceiveItemTextBinding
import com.app.bestbrain.databinding.ChatSendItemBinding
import com.app.bestbrain.models.ChatMessageModel
import com.app.bestbrain.utils.GridSpaceItemDecoration
import com.app.bestbrain.utils.SpaceItemDecoration
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class ChatAdapter(
    private val context: Context,
    private val chatButtonClickListener: ChatButtonAdapter.ChatButtonClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatList = ArrayList<ChatMessageModel>()

    fun addItem(chatMessageModel: ChatMessageModel) {
        chatList.add(0, chatMessageModel)
        notifyDataSetChanged()
    }

    class SendViewHolder(val binding: ChatSendItemBinding) : RecyclerView.ViewHolder(binding.root)

    class ReceiveTextViewHolder(val binding: ChatReceiveItemTextBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ReceiveButtonViewHolder(val binding: ChatReceiveItemButtonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                SendViewHolder(
                    ChatSendItemBinding.inflate(LayoutInflater.from(parent.context))
                )
            }

            2 -> {
                ReceiveTextViewHolder(
                    ChatReceiveItemTextBinding.inflate(LayoutInflater.from(parent.context))
                )
            }

            else -> {
                ReceiveButtonViewHolder(
                    ChatReceiveItemButtonBinding.inflate(LayoutInflater.from(parent.context))
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SendViewHolder -> {
                holder.binding.tvSendMsg.text = Html.fromHtml(chatList[position].data?.bb_value ?: "")

            }

            is ReceiveTextViewHolder -> {
                holder.binding.tvReceiveMsg.text =
                    Html.fromHtml(chatList[position].data?.bb_value ?: "")

            }

            is ReceiveButtonViewHolder -> {
                val chatButtonAdapter = ChatButtonAdapter(
                    context, chatList[position].data?.bb_buttons,
                    chatButtonClickListener
                )
                val layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                    flexWrap = FlexWrap.WRAP
                }
                holder.binding.rvButton.layoutManager = layoutManager
                holder.binding.rvButton.adapter = chatButtonAdapter
            }
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].itemType == 1)
            1
        else {
            if (chatList[position].data?.bb_type.equals("output_text"))
                2
            else
                3
        }
    }
}