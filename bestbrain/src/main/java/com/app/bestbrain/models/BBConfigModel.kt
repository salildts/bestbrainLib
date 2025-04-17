package com.app.bestbrain.models

data class BBConfigModel(
    val apiBase: String?,
    val socketURL: String?,
    val apiKey: String?,
    val appID: String?,
    var headerText: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var authToken: String? = null,
    val userId: Int = 0,
)
