package com.app.bestbrain.models

data class ChatHistoryResponse(
    val `data`: MutableList<ChatMessageModel>?,
    val session_id: String?
)