package com.app.bestbrain.utils

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.TextView
import com.app.bestbrain.R

class ProgressDialog(activity: Activity?, message: String?, cancelable: Boolean?)  {

    private var pDialog: Dialog? = null
    private var tvMessage: TextView? = null

    init {
        pDialog = Dialog(activity!!)
        pDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        pDialog!!.setContentView(R.layout.custom_progress_indicator)
        pDialog!!.setCancelable(cancelable!!)
        pDialog!!.setCanceledOnTouchOutside(cancelable)
        pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tvMessage = pDialog!!.findViewById<View>(R.id.tv_message) as TextView
        tvMessage!!.text = message
    }

    fun setMessage(message: String?) {
        tvMessage!!.text = message
    }

    fun showProgressDialog() {
        try {
            if (pDialog != null) {
                if (!pDialog!!.isShowing) pDialog!!.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideProgressDialog() {
        try {
            if (pDialog != null) {
                if (pDialog!!.isShowing) pDialog!!.dismiss()
                tvMessage!!.text = ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}