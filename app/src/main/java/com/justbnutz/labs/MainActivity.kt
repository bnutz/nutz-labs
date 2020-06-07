package com.justbnutz.labs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.justbnutz.labs.labs.WikiFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewModel()
        initView()
    }

    private fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private fun initView() {
        fragmentPager?.let {
            val pagerAdapter = ScreenPagerAdapter(this)
            it.adapter = pagerAdapter
        }
    }

    override fun onBackPressed() {
        fragmentPager?.let { viewPager ->
            if (viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            } else {
                super.onBackPressed()
            }
        } ?: super.onBackPressed()
    }

    // https://developer.android.com/training/animation/screen-slide-2#kotlin
    private inner class ScreenPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        private val pageList = listOf(
            WikiFragment.newInstance()
        )

        override fun getItemCount(): Int = pageList.size

        override fun createFragment(position: Int) = pageList[position]
    }
}