package com.app.bestbrain.utils

import android.content.Context
import android.content.SharedPreferences
import android.provider.Telephony.Carriers.PASSWORD
import com.app.bestbrain.utils.Constants.Companion.API_KEY

class SharedPreferenceManager(context : Context) {

    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    private var PRIVATE_MODE = 0

    init {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    var sessionId: String?
        get() = pref.getString(SESSION_ID, "")
        set(value) {
            editor.putString(SESSION_ID, value!!)
            editor.commit()

        }

    fun clearAll() {
        editor.clear()
        editor.commit()
    }

    companion object {
        private const val PREF_NAME = "BestBrain"
        private const val SESSION_ID = "session_id"
    }
}