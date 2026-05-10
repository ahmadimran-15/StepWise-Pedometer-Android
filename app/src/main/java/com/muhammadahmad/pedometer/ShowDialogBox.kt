package com.muhammadahmad.pedometer

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button

class ShowDialogBox(private val activity: Activity) {

    private var alertDialog: AlertDialog? = null
    private lateinit var btnYes: Button
    private lateinit var btnNo: Button

    fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val view: View = inflater.inflate(R.layout.show_dialog_exit, null)
        builder.setView(view)





        btnYes = view.findViewById(R.id.btn_yes)
        btnNo = view.findViewById(R.id.btn_no)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCancelable(false) // Make dialog not cancelable by outside touch

        btnYes.setOnClickListener {
            activity.finishAffinity() // Finish the activity only if user confirms
            alertDialog?.dismiss()

        }

        btnNo.setOnClickListener {
            alertDialog?.dismiss() // Dismiss the dialog if user cancels
        }

        alertDialog?.show()
    }
}
