package com.justbnutz.labs.labs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.justbnutz.labs.R
import com.justbnutz.labs.databinding.FragmentCookieBinding
import okhttp3.Cookie

class CookieFragment : BaseFragment<FragmentCookieBinding>() {

    private lateinit var viewModel: CookieViewModel

    private val cookieObserver = Observer<List<Cookie>> {
        binding?.btnLoad?.let {
            parentActivity?.showSnackbar(it, getString(R.string.loaded))
        }
    }

    companion object {
        val TAG: String = this::class.java.name

        fun newInstance() = CookieFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCookieBinding {
        return FragmentCookieBinding.inflate(inflater, container, false)
    }

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
        binding?.editCookie?.setHorizontallyScrolling(true)

        binding?.btnLoad?.setOnClickListener { btnLoad ->
            binding?.editCookie?.text?.let {
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