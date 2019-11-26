package br.com.mdr.mobilechallenge.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import br.com.mdr.mobilechallenge.App
import br.com.mdr.mobilechallenge.R
import br.com.mdr.mobilechallenge.util.Constants.Companion.IMG_REQUEST_CODE
import br.com.mdr.mobilechallenge.util.Constants.Companion.SNACK_DURATION
import com.google.android.material.snackbar.Snackbar

object Utils {

    fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
            (App.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    App.context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    App.context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    App.context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            App.activity?.requestPermissions(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE), IMG_REQUEST_CODE)
        }
    }

    fun hasPermission(permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            return App.context.checkSelfPermission(permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun showSuccesSnack(msg: String) {
        App.activity?.runOnUiThread {
            if (Build.VERSION.SDK_INT >= 23) {
                val snackBar = Snackbar.make(App.view, msg, SNACK_DURATION)
                val textId = com.google.android.material.R.id.snackbar_text
                val snackView = snackBar.view
                try {
                    val params = snackView.layoutParams as CoordinatorLayout.LayoutParams
                    val sideMargin = 16
                    params.setMargins(params.leftMargin + sideMargin,
                        params.topMargin,
                        params.rightMargin + sideMargin,
                        params.bottomMargin + sideMargin)

                    snackView.layoutParams = params
                } catch(e: Exception) {
                    Log.e("App", e.localizedMessage!!)
                }

                snackView.setBackgroundResource(R.drawable.success_snack_corner)

                val txtSnack = snackView.findViewById<TextView>(textId)
                txtSnack.setTextColor(ContextCompat.getColor(App.context, R.color.colorPrimary))
                txtSnack.maxLines = 5
                snackBar.show()
            } else
                showToast(msg)
        }
    }

    fun showErrorSnack(msg: String) {
        if (Build.VERSION.SDK_INT >= 23) {
            val snackBar = Snackbar.make(App.view, msg, SNACK_DURATION)
            val textId = com.google.android.material.R.id.snackbar_text
            val snackView = snackBar.view
            val txtSnack = snackView.findViewById<TextView>(textId)
            txtSnack.maxLines = 5
            val params = snackView.layoutParams as FrameLayout.LayoutParams
            val sideMargin = 16
            params.setMargins(params.leftMargin + sideMargin,
                params.topMargin,
                params.rightMargin + sideMargin,
                params.bottomMargin + sideMargin)
            snackView.layoutParams = params
            snackView.setBackgroundResource(R.drawable.error_snack_corner)

            txtSnack.setTextColor(ContextCompat.getColor(App.context, R.color.colorPrimary))
            snackBar.show()
        } else
            showToast(msg)
    }

    private fun showToast(msg: String) {
        Toast.makeText(App.context, msg, SNACK_DURATION).show()
    }

    fun hideKeyBoard() {
        val inputMethodManager = App.activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(App.activity?.currentFocus?.windowToken, 0)
    }
}