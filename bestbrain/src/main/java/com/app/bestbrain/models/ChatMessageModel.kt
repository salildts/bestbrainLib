package com.app.bestbrain.models

data class ChatMessageModel(
    var `data`: Data? = null,
    val channel: String? = null,
    val channel_id: Any? = null,
    val chat_thread_id: String? = null,
    val message: Any? = null,
    val role: String? = null,
    var session_id: String? = null,
    val ts: String? = null,
    var itemType: Int = 0
) {
    data class Data(
        val bb_buttons: List<BbButton?>? = null,
        var bb_type: String? = null,
        var bb_value: Any? = null,
        val bb_options: List<BbOption>? = null,
        val bb_items: List<BbOption>? = null,
        var enable: Boolean = true
    ) {
        data class BbButton(
            val bb_actions: List<BbAction?>? = null,
            val label: String? = null,
            val source: String? = null,
            val value: String? = null,
            var enable: Boolean = true
        ) {
            data class BbAction(
                val target_block: String? = null,
                val target_element: String? = null,
                val target_workflow: String? = null,
                val type: String? = null
            )
        }
    }
}