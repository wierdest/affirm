package me.wierdest.affirmative.utilities

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.widget.ProgressBar
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat

class RedMaxProgressBar(context: Context, attributeSet: AttributeSet) : ProgressBar(context, attributeSet) {

    private val TAG = javaClass.name
    private val indeDrawable = indeterminateDrawable

    // if we wanted to set the actual progress layer to red, we would do:

//    private val drawable = progressDrawable.mutate() as LayerDrawable
//    private val progDrawable = drawable.findDrawableByLayerId(android.R.id.progress)

    // setting a color filter seems more intuitive.

    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        if (progress == max) {
            Log.i(TAG, "bar is at maximum!")
            indeDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.RED, BlendModeCompat.SRC_ATOP)
//            progDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.RED, BlendModeCompat.SRC_ATOP)
            isIndeterminate = true

        } else {
//            progDrawable?.clearColorFilter()
            indeDrawable?.clearColorFilter()
            isIndeterminate = false
        }

    }

}