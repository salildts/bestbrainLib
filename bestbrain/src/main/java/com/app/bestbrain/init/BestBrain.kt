package com.app.bestbrain.init

import com.app.bestbrain.utils.Constants
import org.json.JSONObject

class BestBrain {

    companion object {

        fun initialize(configuration: JSONObject) {
            Constants.API_BASE_URL = configuration.optString("apiBase")
            Constants.SOCKET_URL = configuration.optString("socketURL")
            Constants.API_KEY = configuration.optString("appKey")
            Constants.APP_ID = configuration.optString("appID")
            Constants.HEADER_TEXT = configuration.optString("headerText")
            Constants.NAME = configuration.optString("name")
        }
    }
}