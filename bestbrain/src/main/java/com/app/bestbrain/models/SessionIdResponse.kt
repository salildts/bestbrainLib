package com.app.bestbrain.models

data class SessionIdResponse(
    val `data`: Data,
    val session_id: String
)

data class Data(
    val app_id: String,
    val audio_call_id: String,
    val audio_stream_id: String,
    val browser: String,
    val city: String,
    val conversation_background: String,
    val country: String,
    val current_block: String,
    val current_index: Int,
    val current_workflow: String,
    val device: String,
    val fingerprint: String,
    val language: Language,
    val os: String,
    val page_domain: String,
    val page_url: String,
    val region: String,
    val session_id: String,
    val thread_id: String,
    val type: String,
    val user_agent: String,
    val user_identifier: String,
    val variables: Variables
)

data class Language(
    val source: String,
    val target: String,
    val type: String
)

data class Variables(
    val bb_agent_name: String
)