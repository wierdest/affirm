package me.wierdest.affirmative.utilities


import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


/**
 * Collection of Fragment extension functions
 */

val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

/**
 * Hides activity's bars
 */
fun Fragment.hideSupportBar(h: Boolean)  {
    val castActivity = this.activity as AppCompatActivity
    val bar = castActivity.supportActionBar
    if (h) bar?.hide() else bar?.show()
}
/**
 * Sets supportBar's title
 */

fun Fragment.setSupportBarTitle(t: String) {
    val castActivity = this.activity as AppCompatActivity
    castActivity.supportActionBar?.title = t
}

