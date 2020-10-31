package me.wierdest.affirmative.utilities

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.ViewPropertyAnimator
import com.google.android.material.textview.MaterialTextView

class TypewriterTextView(context: Context) : MaterialTextView(context) {

    private var writerHandler = Handler()
    private var toWrite: CharSequence = ""
    private var currIndex: Int = 0
    private var delay: Long = 100L
    private lateinit var post: () -> Unit

    private val writerRunnable = object: Runnable {
        override fun run() {
            if (currIndex <= toWrite.length) {
                setText(toWrite.subSequence(startIndex = 0, endIndex = currIndex++))
                writerHandler.postDelayed(this, delay)
            } else {
                writerHandler.removeCallbacks(this)
                post()
            }
        }
    }

    fun animateText(toAnimate: CharSequence, postFunction: () -> Unit) {
        toWrite = toAnimate
        currIndex = 0
        post = postFunction
        writerRunnable.run()
    }

    fun setCharacterDelay(delayInMilli: Long) {
        delay = delayInMilli
    }

    override fun animate(): ViewPropertyAnimator {
        return super.animate()
    }


}