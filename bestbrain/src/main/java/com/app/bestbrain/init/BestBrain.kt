package com.app.bestbrain.init

import com.app.bestbrain.utils.Constants

class BestBrain {

    companion object {

        fun initializeBestBrain(baseUrl: String, socketUrl: String, apiKey: String, appId: String) {
            Constants.API_BASE_URL = baseUrl
            Constants.SOCKET_URL = socketUrl
            Constants.API_KEY = apiKey
            Constants.APP_ID = appId
        }
    }
}