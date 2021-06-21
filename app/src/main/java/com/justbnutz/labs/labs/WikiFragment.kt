package com.justbnutz.labs.labs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.justbnutz.labs.R
import com.justbnutz.labs.databinding.FragmentWikipediaBinding

class WikiFragment : BaseFragment<FragmentWikipediaBinding>() {

    private lateinit var viewModel: WikiViewModel

    // A LiveData observer which updates the UI on changes
    private val responseObserver = Observer<String> {response ->
        binding?.txtResponse?.let {
            it.append(response)
            it.append("\n")
            it.postDelayed({
                binding?.scrollResponse?.fullScroll(View.FOCUS_DOWN)
            }, 500)
        }
    }

    companion object {
        val TAG = "${this::class.qualifiedName}"

        fun newInstance() = WikiFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWikipediaBinding {
        return FragmentWikipediaBinding.inflate(inflater, container, false)
    }

    override fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel = ViewModelProvider(this).get(WikiViewModel::class.java)

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer
        viewModel.response.observe(this, responseObserver)
    }

    override fun initView() {
        binding?.btnSubmit?.setOnClickListener { btnView ->
            binding?.editSearch?.text?.toString()?.let {
                if (it.isNotEmpty()) {
                    parentActivity?.hideKeyboard(btnView)
                    parentActivity?.showSnackbar(btnView, getString(R.string.loading))
                    viewModel.beginSearch(it)
                }
           }
        }

        // Turns off word-wrap in TextView
        binding?.txtResponse?.setHorizontallyScrolling(true)
    }
}