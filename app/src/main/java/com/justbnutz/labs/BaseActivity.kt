package com.justbnutz.labs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Common methods between all activities go here
 */
abstract class BaseActivity : AppCompatActivity() {

    fun hideKeyboard(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let {
            view.windowToken?.let { token ->
                it.hideSoftInputFromWindow(token, 0)
            }
        }
    }

    fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun onItemCopy(copyText: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?)?.let {
            val clip = ClipData.newPlainText("text", copyText)
            it.setPrimaryClip(clip)

            showToast(getString(R.string.item_copied))
        }
    }

    fun onItemShare(shareText: String) {
        val shareTitle = getString(R.string.share_title)
        val intent = Intent(Intent.ACTION_SEND).also {
            it.type = "text/plain"
            it.putExtra(Intent.EXTRA_SUBJECT, shareTitle)
            it.putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, null))
    }
}