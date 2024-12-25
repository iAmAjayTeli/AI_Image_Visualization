package com.example.myconnect.handler

import android.app.Activity
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.myconnect.R

class LoadingDialog(val mActivity: Activity, val text: String) {

    private lateinit var isDialog: AlertDialog

    fun startLoading() {
        //set view
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_bar, null)

        val dialogTxt = dialogView.findViewById<TextView>(R.id.txt)

        dialogTxt.text = text

        //set dialog
        val builder = AlertDialog.Builder(mActivity, R.style.CustomAlertDialog)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog.show()

    }

    fun isDismiss() {
        isDialog.dismiss()
    }
}