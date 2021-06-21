package com.justbnutz.labs

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.justbnutz.labs.databinding.ActivityMainBinding
import com.justbnutz.labs.labs.AppListFragment
import com.justbnutz.labs.labs.CookieFragment
import com.justbnutz.labs.labs.IgFragment
import com.justbnutz.labs.labs.WikiFragment

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var viewModel: MainViewModel

    private val pagerCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            (binding.fragmentPager.adapter as? ScreenPagerAdapter)?.let {
                showToast(it.getFragmentTag(position).removePrefix(BuildConfig.APPLICATION_ID).removeSuffix(".Companion"))
            }
        }
    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun onResume() {
        super.onResume()
        binding.fragmentPager.registerOnPageChangeCallback(pagerCallback)
    }

    override fun onPause() {
        binding.fragmentPager.unregisterOnPageChangeCallback(pagerCallback)
        super.onPause()
    }

    override fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun initView() {
        binding.fragmentPager.let {
            val pagerAdapter = ScreenPagerAdapter(this)
            it.adapter = pagerAdapter
        }
    }

    override fun onBackPressed() {
        binding.fragmentPager.let { viewPager ->
            if (viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            } else {
                super.onBackPressed()
            }
        }
    }

    // https://developer.android.com/training/animation/screen-slide-2#kotlin
    private inner class ScreenPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        private val pageList = listOf(
            Pair(AppListFragment.newInstance(), AppListFragment.TAG),
            Pair(WikiFragment.newInstance(), WikiFragment.TAG),
            Pair(CookieFragment.newInstance(), CookieFragment.TAG),
            Pair(IgFragment.newInstance(), IgFragment.TAG)
        )

        fun getFragmentTag(position: Int) = pageList[position].second

        override fun getItemCount(): Int = pageList.size

        override fun createFragment(position: Int) = pageList[position].first
    }
}