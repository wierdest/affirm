package me.wierdest.affirmative.reader.listeners

import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.forEach
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.wierdest.affirmative.R
import me.wierdest.affirmative.databinding.FragmentReaderBinding

class ReaderControls(private val b: FragmentReaderBinding, private val decrease: Boolean) : View.OnClickListener {

    override fun onClick(v: View?) {
        val currentSnippetId = b.snippet!!.snippet.snippetId
        // to read next, we increase the SnipCount in the viewModel:
        b.myViewModel!!.snippetHandler.updateSnipCount(currentSnippetId, decrease = decrease)
        // we empty the text in the switcher, to trigger an animation on the next text view and change
        // the animation to the right direction
        b.readerTextSwitcher.apply {
            outAnimation = if (decrease) {
                AnimationUtils.loadAnimation(context, R.anim.slide_out_left)
            } else {
                AnimationUtils.loadAnimation(context, R.anim.slide_out_right)
            }
            setText("")
            hideFabs()
        }

    }

    private fun hideFabs() {
        b.readerLayout.forEach {
            if (it is ExtendedFloatingActionButton) {
                if (it.visibility != View.GONE) {
                    it.visibility = View.INVISIBLE
                }
            }
            if (it is FloatingActionButton) {
                it.visibility = View.INVISIBLE
            }
        }
    }

}