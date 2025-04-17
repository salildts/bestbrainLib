package com.app.bestbrain.init

import com.app.bestbrain.models.BBConfigModel

object BBInit {

    private lateinit var config: BBConfigModel

    fun initialize(configuration: BBConfigModel) {
        config = configuration
    }

    fun getConfig(): BBConfigModel {
        return config
    }
}