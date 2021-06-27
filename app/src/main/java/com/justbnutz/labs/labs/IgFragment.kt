package com.justbnutz.labs.labs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.justbnutz.labs.R
import com.justbnutz.labs.databinding.FragmentInstagramBinding
import okhttp3.Cookie

class IgFragment : BaseFragment<FragmentInstagramBinding>() {

    private lateinit var viewModel: IgViewModel
    private lateinit var vmCookie: CookieViewModel

    // A LiveData observer which updates the UI on changes
    private val responseObserver = Observer<String> { response ->
        binding?.txtResponse?.let {
            it.append(response)
            it.append("\n")
            it.postDelayed({
                binding?.scrollResponse?.fullScroll(View.FOCUS_DOWN)
            }, 500)
        }
    }

    private val cookieObserver = Observer<List<Cookie>> {
        viewModel.updateCookies(it)
        it?.forEach {
            when(it.name) {
                viewModel.DS_USER_ID -> binding?.editDsUserId?.setText(it.value)
                viewModel.SESSIONID -> binding?.editSessionid?.setText(it.value)
                viewModel.CSRFTOKEN -> binding?.editCsrftoken?.setText(it.value)
            }
        }
    }

    companion object {
        val TAG: String = this::class.java.name

        fun newInstance() = IgFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentInstagramBinding {
        return FragmentInstagramBinding.inflate(inflater, container, false)
    }

    override fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel = ViewModelProvider(this).get(IgViewModel::class.java)

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer
        viewModel.response.observe(this, responseObserver)

        parentActivity?.apply {
            // CookieViewModel is owned by parent activity to ensure we get the same instance
            vmCookie = ViewModelProvider(this).get(CookieViewModel::class.java)
            vmCookie.cookieList.observe(this@IgFragment, cookieObserver)
        }
    }

    override fun initView() {
        binding?.btnSubmit?.setOnClickListener { btnView ->
            if (binding?.editTargetUsername?.text?.isNotBlank() == true
                && vmCookie.cookieList.value?.isNotEmpty() == true) {
                parentActivity?.hideKeyboard(btnView)
                parentActivity?.showSnackbar(btnView, getString(R.string.loading))
                viewModel.getFollowing(binding?.editTargetUsername?.text.toString())
            } else {
                parentActivity?.showSnackbar(btnView, getString(R.string.incomplete_form))
            }
        }

        // Turns off word-wrap in TextView
        binding?.txtResponse?.setHorizontallyScrolling(true)
    }
}