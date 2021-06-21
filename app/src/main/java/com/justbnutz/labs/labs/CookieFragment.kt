package com.justbnutz.labs.labs

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.justbnutz.labs.R
import kotlinx.android.synthetic.main.fragment_cookie.*
import okhttp3.Cookie

class CookieFragment : BaseFragment() {

    private lateinit var viewModel: CookieViewModel

    private val cookieObserver = Observer<List<Cookie>> {
        parentActivity?.showSnackbar(btn_load, getString(R.string.loaded))
    }

    companion object {
        val TAG = "${this::class.qualifiedName}"

        fun newInstance() = CookieFragment()
    }

    override fun getLayoutId() = R.layout.fragment_cookie

    override fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        parentActivity?.apply {
            // Let the parent activity own the ViewModel so that it can be reused by other Fragments
            viewModel = ViewModelProvider(this).get(CookieViewModel::class.java)
            viewModel.cookieList.observe(this@CookieFragment, cookieObserver)
        }
    }

    override fun initView() {
        // Turns off word-wrap in EditText
        edit_cookie?.setHorizontallyScrolling(true)

        btn_load?.setOnClickListener { btnLoad ->
            edit_cookie?.text?.let {
                if (it.isNotBlank()) {
                    parentActivity?.hideKeyboard(btnLoad)
                    parentActivity?.showSnackbar(btnLoad, getString(R.string.loading))
                    viewModel.loadCookie(it.toString())
                } else {
                    parentActivity?.showSnackbar(btnLoad, getString(R.string.incomplete_form))
                }
            }
        }
    }
}