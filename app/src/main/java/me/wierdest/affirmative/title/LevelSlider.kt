package me.wierdest.affirmative.title

import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import me.wierdest.affirmative.databinding.FragmentTitleBinding
import me.wierdest.affirmative.utilities.TypewriterTextView
import me.wierdest.affirmative.utilities.prepareForComparison
import java.util.*

class LevelSlider(private val b: FragmentTitleBinding) : SeekBar.OnSeekBarChangeListener {
    val TAG = javaClass.name
    private var startProgress: Int? = null
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        startProgress = seekBar?.progress
        b.titleLayout.transitionToStart()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        processLevelChoice(seekBar?.progress!!)

    }

    private fun processLevelChoice(progress: Int) {
        val progressString = when (progress) {
            in 1..30 -> { "A1-A2" }
            in 31..49 -> { "B1" }
            in 50..65 -> { "B2" }
            in 65..90 -> { "C1" }
            in 90..100 -> { "C1-C2" }
            else -> { "Random" }
        }
        b.myViewModel!!.snippetHandler.pickSnippetToReadByLevel(progressString.prepareForComparison())
        b.titleLayout.transitionToEnd()
    }

}