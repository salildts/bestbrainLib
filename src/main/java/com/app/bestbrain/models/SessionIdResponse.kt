package com.app.bestbrain.models

data class SessionIdResponse(
    var `data`: Data? = null,
    var session_id: String? = null
) {
    data class Data(
        var app_id: String? = null,
        var current_block: String? = null,
        var current_index: Int? = null,
        var current_workflow: String? = null,
        var session_id: String? = null,
        var thread_id: Any? = null,
        var type: String? = null,
        var user_agent: Any? = null,
        var variables: Variables? = null
    ) {
        class Variables
    }
}