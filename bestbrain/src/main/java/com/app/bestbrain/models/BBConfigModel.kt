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
    val cmmsApiKey: String? = null,
    val companyId: String? = null,
)

class BBConfigBuilder {
    private var apiBase: String? = null
    private var socketURL: String? = null
    private var apiKey: String? = null
    private var appID: String? = null
    private var headerText: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var authToken: String? = null
    private var userId: Int = 0
    private var cmmsApiKey: String? = null
    private var companyId: String? = null

    fun apiBase(value: String?) = apply { apiBase = value }
    fun socketURL(value: String?) = apply { socketURL = value }
    fun apiKey(value: String?) = apply { apiKey = value }
    fun appID(value: String?) = apply { appID = value }
    fun headerText(value: String?) = apply { headerText = value }
    fun firstName(value: String?) = apply { firstName = value }
    fun lastName(value: String?) = apply { lastName = value }
    fun email(value: String?) = apply { email = value }
    fun authToken(value: String?) = apply { authToken = value }
    fun userId(value: Int) = apply { userId = value }
    fun cmmsApiKey(value: String?) = apply { cmmsApiKey = value }
    fun companyId(value: String?) = apply { companyId = value }

    fun build(): BBConfigModel {
        return BBConfigModel(
            apiBase,
            socketURL,
            apiKey,
            appID,
            headerText,
            firstName,
            lastName,
            email,
            authToken,
            userId,
            cmmsApiKey,
            companyId
        )
    }
}
