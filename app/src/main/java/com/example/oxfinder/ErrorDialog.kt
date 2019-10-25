package com.example.oxfinder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment

@SuppressLint("ValidFragment")
class ErrorDialog (msg : String) : DialogFragment() {
    val messageStr = msg;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        builder.setMessage(messageStr)
            .setTitle(R.string.ui_error)
            .setPositiveButton(R.string.ui_ok, DialogInterface.OnClickListener{ dialog, _ ->
                dialog.dismiss()
            })

        return builder.create()
    }
}