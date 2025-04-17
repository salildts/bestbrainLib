package com.app.bestbrain.adapter

import android.content.Context
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.bestbrain.databinding.ChatAttachFileBinding
import com.app.bestbrain.databinding.ChatReceiveItemButtonBinding
import com.app.bestbrain.databinding.ChatReceiveItemDropdownBinding
import com.app.bestbrain.databinding.ChatReceiveItemTextBinding
import com.app.bestbrain.databinding.ChatScanQrBinding
import com.app.bestbrain.databinding.ChatSendItemBinding
import com.app.bestbrain.models.BbOption
import com.app.bestbrain.models.ChatMessageModel
import com.app.bestbrain.utils.BBType
import com.app.bestbrain.utils.CommonMethods
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class ChatAdapter(
    private val context: Context,
    val onButtonClick: (String) -> Unit,
    val onQrClick: (Int) -> Unit,
    val onFileClick: (Int) -> Unit,
    val onDropdownClick: (List<BbOption>?, Int) -> Unit,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatList = mutableListOf<ChatMessageModel>()

    fun getChatList(): MutableList<ChatMessageModel> {
        return chatList
    }

    fun addItem(chatMessageModel: ChatMessageModel) {
        chatList.add(0, chatMessageModel)
        notifyDataSetChanged()
    }

    fun addAllItem(chatMessageModel: List<ChatMessageModel>) {
        chatList.addAll(chatMessageModel)
        notifyDataSetChanged()
    }

    class SendViewHolder(val binding: ChatSendItemBinding) : RecyclerView.ViewHolder(binding.root)

    class ReceiveTextViewHolder(val binding: ChatReceiveItemTextBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ReceiveButtonViewHolder(val binding: ChatReceiveItemButtonBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ScanQRViewHolder(val binding: ChatScanQrBinding) : RecyclerView.ViewHolder(binding.root)

    class AttachFileViewHolder(val binding: ChatAttachFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DropdownViewHolder(val binding: ChatReceiveItemDropdownBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                SendViewHolder(
                    ChatSendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            2 -> {
                ReceiveTextViewHolder(
                    ChatReceiveItemTextBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            3 -> {
                ScanQRViewHolder(
                    ChatScanQrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            4 -> {
                AttachFileViewHolder(
                    ChatAttachFileBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            5 -> {
                DropdownViewHolder(
                    ChatReceiveItemDropdownBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                ReceiveButtonViewHolder(
                    ChatReceiveItemButtonBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SendViewHolder -> {
                if (chatList[position].data?.bb_value is String)
                    holder.binding.tvSendMsg.text = Html.fromHtml(
                        (chatList[position].data?.bb_value ?: "") as String?,
                        FROM_HTML_MODE_LEGACY
                    ).trim()
                else if (CommonMethods.isNonStringPrimitive(chatList[position].data?.bb_value))
                    holder.binding.tvSendMsg.text = chatList[position].data?.bb_value.toString()
                else
                    holder.binding.tvSendMsg.text = ""

            }

            is ReceiveTextViewHolder -> {
                if (chatList[position].data?.bb_value is String)
                    holder.binding.tvReceiveMsg.text =
                        Html.fromHtml(
                            (chatList[position].data?.bb_value ?: "") as String?,
                            FROM_HTML_MODE_LEGACY
                        ).trim()
                else if (CommonMethods.isNonStringPrimitive(chatList[position].data?.bb_value))
                    holder.binding.tvReceiveMsg.text = chatList[position].data?.bb_value.toString()
                else
                    holder.binding.tvReceiveMsg.text = ""
            }

            is ReceiveButtonViewHolder -> {
                val chatButtonAdapter = ChatButtonAdapter(
                    context, chatList[position].data?.bb_buttons,
                    onButtonClick
                )
                val layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                    flexWrap = FlexWrap.WRAP
                }
                holder.binding.rvButton.layoutManager = layoutManager
                holder.binding.rvButton.adapter = chatButtonAdapter
            }

            is ScanQRViewHolder -> {
                holder.binding.cardQrScan.setOnClickListener {
                    if (chatList[position].data?.enable == true)
                        onQrClick(position)
                }
            }

            is AttachFileViewHolder -> {
                holder.binding.cardAttachFile.setOnClickListener {
                    if (chatList[position].data?.enable == true)
                        onFileClick(position)
                }
            }

            is DropdownViewHolder -> {
                holder.binding.edtDropdown.setOnClickListener {
                    if (chatList[position].data?.enable == true)
                        onDropdownClick(
                            chatList[position].data?.bb_options
                                ?: chatList[position].data?.bb_items, position
                        )
                }
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
            if (chatList[position].data?.bb_type.equals(BBType.output_text.toString()))
                2
            else if (chatList[position].data?.bb_type.equals(BBType.input_qr_code.toString()))
                3
            else if (chatList[position].data?.bb_type.equals(BBType.input_file.toString()))
                4
            else if (chatList[position].data?.bb_type.equals(BBType.input_dropdown.toString()))
                5
            else
                6
        }
    }
}