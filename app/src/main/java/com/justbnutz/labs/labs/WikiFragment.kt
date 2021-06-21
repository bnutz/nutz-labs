package com.justbnutz.labs.labs

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.justbnutz.labs.R
import kotlinx.android.synthetic.main.fragment_wikipedia.*

class WikiFragment : BaseFragment() {

    private lateinit var viewModel: WikiViewModel

    // A LiveData observer which updates the UI on changes
    private val responseObserver = Observer<String> {response ->
        txt_response?.let {
            it.append(response)
            it.append("\n")
            it.postDelayed({
                scroll_response?.fullScroll(View.FOCUS_DOWN)
            }, 500)
        }
    }

    companion object {
        val TAG = "${this::class.qualifiedName}"

        fun newInstance() = WikiFragment()
    }

    override fun getLayoutId() = R.layout.fragment_wikipedia

    override fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel = ViewModelProvider(this).get(WikiViewModel::class.java)

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer
        viewModel.response.observe(this, responseObserver)
    }

    override fun initView() {
        btn_submit?.setOnClickListener { btnView ->
            edit_search?.text?.toString()?.let {
                if (it.isNotEmpty()) {
                    parentActivity?.hideKeyboard(btnView)
                    parentActivity?.showSnackbar(btnView, getString(R.string.loading))
                    viewModel.beginSearch(it)
                }
           }
        }

        // Turns off word-wrap in TextView
        txt_response?.setHorizontallyScrolling(true)
    }
}