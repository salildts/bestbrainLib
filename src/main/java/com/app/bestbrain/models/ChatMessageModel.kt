package com.app.bestbrain.models

data class ChatMessageModel(
    var `data`: Data? = null,
    var session_id: String? = null,
    var thread_id: Any? = null,
    var itemType: Int = 0
) {
    data class Data(
        val bb_buttons: List<BbButton?>? = null,
        var bb_type: String? = null,
        var bb_value: String? = null
    ) {
        data class BbButton(
            val bb_actions: List<BbAction?>? = null,
            val label: String? = null,
            val source: String? = null,
            val value: String? = null,
            var enable : Boolean = true
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