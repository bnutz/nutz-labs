package com.justbnutz.labs.labs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.justbnutz.labs.BaseActivity

/**
 * Common methods between all fragments go here
 */
abstract class BaseFragment : Fragment() {

    val parentActivity by lazy { activity as? BaseActivity }

    @LayoutRes
    abstract fun getLayoutId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    abstract fun initViewModel()

    abstract fun initView()
}