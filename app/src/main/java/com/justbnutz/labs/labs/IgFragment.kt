package com.justbnutz.labs.labs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.justbnutz.labs.R
import kotlinx.android.synthetic.main.fragment_instagram.*
import okhttp3.Cookie

class IgFragment : BaseFragment() {

    private lateinit var viewModel: IgViewModel
    private lateinit var vmCookie: CookieViewModel

    // A LiveData observer which updates the UI on changes
    private val responseObserver = Observer<String> { response ->
        txt_response?.let {
            it.append(response)
            it.append("\n")
            it.postDelayed({
                scroll_response?.fullScroll(View.FOCUS_DOWN)
            }, 500)
        }
    }

    private val cookieObserver = Observer<List<Cookie>> {
        viewModel.updateCookies(it)
        it?.forEach {
            when(it.name) {
                viewModel.DS_USER_ID -> edit_ds_user_id?.setText(it.value)
                viewModel.SESSIONID -> edit_sessionid?.setText(it.value)
                viewModel.CSRFTOKEN -> edit_csrftoken?.setText(it.value)
            }
        }
    }

    companion object {
        val TAG = "${this::class.qualifiedName}"

        fun newInstance() = IgFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_instagram, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    private fun initViewModel() {
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

    private fun initView() {
        btn_submit?.setOnClickListener { btnView ->
            if (edit_target_username?.text?.isNotBlank() == true
                && vmCookie.cookieList.value?.isNotEmpty() == true) {
                parentActivity?.hideKeyboard(btnView)
                parentActivity?.showSnackbar(btnView, getString(R.string.loading))
                viewModel.getFollowing(edit_target_username.text.toString())
            } else {
                parentActivity?.showSnackbar(btnView, getString(R.string.incomplete_form))
            }
        }

        txt_response?.let { txtView ->
            // Turns off word-wrap in TextView
            txtView.setHorizontallyScrolling(true)

            txtView.setOnLongClickListener { _ ->
                txtView.text?.toString()?.let {
                    parentActivity?.onItemCopy(it)
                }
                true
            }
        }
    }
}