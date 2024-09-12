package com.app.bestbrain.init

import com.app.bestbrain.utils.Constants
import org.json.JSONObject

class BestBrain {

    companion object {

        fun initialize(configuration: JSONObject) {
            Constants.API_BASE_URL = configuration.getString("apiBase")
            Constants.SOCKET_URL = configuration.getString("socketURL")
            Constants.API_KEY = configuration.getString("appKey")
            Constants.APP_ID = configuration.getString("appID")
        }
    }
}