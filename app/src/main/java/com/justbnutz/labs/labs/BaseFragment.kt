package com.justbnutz.labs.labs

import androidx.fragment.app.Fragment
import com.justbnutz.labs.BaseActivity

/**
 * Common methods between all fragments go here
 */
abstract class BaseFragment : Fragment() {

    val parentActivity by lazy { activity as? BaseActivity }
}