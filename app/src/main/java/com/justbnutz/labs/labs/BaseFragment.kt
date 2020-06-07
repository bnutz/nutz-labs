package com.justbnutz.labs.labs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.justbnutz.labs.R
import org.jetbrains.anko.share

abstract class BaseFragment : Fragment() {

    fun hideKeyboard(view: View) {
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let {
            view.windowToken?.let { token ->
                it.hideSoftInputFromWindow(token, 0)
            }
        }
    }
    
    fun onItemCopy(copyText: String) {
        (activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?)?.let {
            val clip = ClipData.newPlainText("text", copyText)
            it.setPrimaryClip(clip)

            Toast.makeText(activity, R.string.item_copied, Toast.LENGTH_SHORT).show()
        }
    }

    fun onItemShare(shareText: String) {
        val shareTitle = getString(R.string.share_title)
        activity?.share(shareText, shareTitle)
    }
}