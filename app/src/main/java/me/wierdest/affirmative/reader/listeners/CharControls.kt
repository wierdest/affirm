package me.wierdest.affirmative.reader.listeners

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import me.wierdest.affirmative.R
import me.wierdest.affirmative.database.Snip
import me.wierdest.affirmative.databinding.FragmentReaderBinding

@Suppress("InlinedApi")
class CharControls(private val b: FragmentReaderBinding) : View.OnTouchListener {
    private val TAG = javaClass.name

    private val bar = b.readerNegBonusProgressBar
    private val barIncreaseAnim: ObjectAnimator = ObjectAnimator.ofInt(bar, "progress", bar.progress, 100)

    private var startTime = 0L

    init {
        barIncreaseAnim.duration = 400 // this value is the limit for the delta modifier bonus
        barIncreaseAnim.addUpdateListener { ValueAnimator.AnimatorUpdateListener { bar.progress = it.animatedValue as Int } }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.performClick()
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val button = v as ExtendedFloatingActionButton
                button.icon = ContextCompat.getDrawable(button.context, R.drawable.visibility_off_24px)
                b.readerTextSwitcher.apply {
                    outAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
                    inAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                    setText(
                        when(v.tag) {
                            "full" -> { b.snip!!.text}
                            "double" -> { b.snip!!.textHidSingle }
                            else -> { b.snip!!.textHidDouble }
                        }
                    )
                }
                bar.visibility = View.VISIBLE
                barIncreaseAnim.start()
                b.controlLayout.transitionToStart()
//                hideReaderFabs()

                startTime = System.currentTimeMillis()


            }
            MotionEvent.ACTION_UP -> {
                val button = v as ExtendedFloatingActionButton
                button.icon = ContextCompat.getDrawable(button.context, R.drawable.visibility_24px)
                b.readerTextSwitcher.setText(pickSnipTextToRead(b.snip!!))
                val endTime = System.currentTimeMillis()
                b.myViewModel!!.snippetHandler.updateSnipDeltaMod(b.snippet!!.snippet.snippetId, endTime - startTime)
                barIncreaseAnim.cancel()
                b.controlLayout.transitionToEnd()
//                showReaderFabs(b.snippet!!.snippet.snipCount)
                bar.visibility = View.INVISIBLE

            }

        }

        return v?.onTouchEvent(event) ?: true

    }

    private fun pickSnipTextToRead(snip: Snip) : String {
        return when (b.snippet!!.snippet.hideMode) {
            'S' -> { snip.textHidSingle }
            'D' -> { snip.textHidDouble }
            'T' -> { snip.textHidTriple }
            else -> snip.text
        }
    }

    private fun hideReaderFabs() {
        b.apply {
            readPreviousFab.visibility = View.INVISIBLE
            readNextFab.visibility = View.INVISIBLE
            readOutLoudFab.visibility = View.INVISIBLE
        }
    }
    private fun showReaderFabs(snipCount: Int) {
        if (snipCount != 0) {
            b.readPreviousFab.show()
        }
        // show read out loud and read next fab
        b.readOutLoudFab.show()
        b.readNextFab.show()
    }

}