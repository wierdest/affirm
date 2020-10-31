package me.wierdest.affirmative.reader

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Path
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.transition.*
import me.wierdest.affirmative.R
import me.wierdest.affirmative.database.MyViewModel
import me.wierdest.affirmative.database.Snip
import me.wierdest.affirmative.databinding.FragmentReaderBinding
import me.wierdest.affirmative.reader.listeners.CharControls
import me.wierdest.affirmative.reader.listeners.ReadOutLoud
import me.wierdest.affirmative.reader.listeners.ReaderControls
import me.wierdest.affirmative.reader.listeners.Speech
import me.wierdest.affirmative.utilities.TypewriterTextView
import me.wierdest.affirmative.utilities.hideSupportBar
import me.wierdest.affirmative.utilities.setSupportBarTitle

class ReaderFragment : Fragment() {
    val TAG = javaClass.name
    private lateinit var b: FragmentReaderBinding
    private lateinit var viewModel: MyViewModel
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var preferences: SharedPreferences
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    @SuppressLint("ClickableViewAccessibility")
    @Suppress("InlinedApi")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /**
         * Binding and viewModel
         */
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_reader, container, false)
        b.lifecycleOwner = this
        viewModel = ViewModelProvider(this.requireActivity()).get(MyViewModel::class.java)
        b.myViewModel = viewModel

        ViewCompat.setTransitionName(b.readOutLoudFab, "shared_element_end_fab")

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 1000
            scrimColor = Color.TRANSPARENT
        }

        /**
         * Shared preferences
         */
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        /**
         * Speech recognizer
         */
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizer.setRecognitionListener(Speech(b))
        /**
         * Read out loud fab listener
         */
        b.readOutLoudFab.setOnClickListener(ReadOutLoud(b, speechRecognizer))
        /**
         * Reader controls - previous & next fabs
         */
        b.readNextFab.setOnClickListener(ReaderControls(b, false))
        b.readPreviousFab.setOnClickListener(ReaderControls(b, true))

        /**
         * Show char fab listeners
         */
        b.readerShowFullFab.setOnTouchListener(CharControls(b))
        b.readerShowDoubleFab.setOnTouchListener(CharControls(b))
        b.readerShowSingleFab.setOnTouchListener(CharControls(b))

        /**
         * TextSwitcher
         */
        b.readerTextSwitcher.apply {
            // we actually don't need a slide in animation, because we're animating each character, like a typewriter, in our
            // custom text view, so when the new text view enters the fragment, it is blank.
            outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right)
            setFactory {
                val textView = TypewriterTextView(context).apply {
                    textSize = 36F
                    gravity = Gravity.CENTER
                    typeface = Typeface.defaultFromStyle(Typeface.ITALIC)
                    setTextColor(ResourcesCompat.getColor(resources, R.color.colorWhite, null))
                }
                textView
            }
        }
        viewModel.snippetHandler.snippetToRead.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                b.snippet = it
                if (it.snips.isEmpty()) {
                    Log.i(TAG, "snips are empty")
                } else {
                    setSupportBarTitle(it.snippet.name)
                    b.readerSnipImage.setImageResource(it.snippet.icon)
                    val snipCount = it.snippet.snipCount
                    // update snip counter text
                    Log.i(TAG, "snipCount: $snipCount")
                    b.readerSnipCounter.text = getString(R.string.frag_reader_snip_counter, snipCount + 1, it.snips.size)
                    if (snipCount < it.snips.size) {
                        val currentSnip = it.snips[snipCount]
                        b.snip = currentSnip
                        val sessionCharDelay = it.snippet.charDelay
                        val sessionCharMode = it.snippet.hideMode
                        setUpShowFabsText(sessionCharMode, currentSnip.charsToHide)

                        Log.i(TAG, "current snip: $currentSnip")
                        if (currentSnip.spoken.isEmpty()) {
                            // BEFORE USER INPUT AUDIO
                            // if we haven't a spoken text [ this changes inside the speech listener and the value is reset to
                            // empty when the user hits the 'previous' fab]
                            val textView = b.readerTextSwitcher.currentView as TypewriterTextView
                            textView.setCharacterDelay(sessionCharDelay)
                            textView.apply {
                                animateText(currentSnip.text) {
                                    countDownToHideAndStartPractice(400, this, pickSnipTextToRead(currentSnip), sessionCharMode, it.snippet.snippetId)
                                }
                            }
                        } else {
                            // AFTER USER INPUT AUDIO
                            // show the results of the snip read, with an option to not show the warning again
                            Log.i(TAG, "Snip read! Accuracy result: ${currentSnip.accuracy}")

                        }
                    } else {
                        Log.i(TAG, "Done reading a full snippet!!")
                    }
                }
            }
        })


        return b.root

    }

    private fun countDownToHideAndStartPractice(waitToHide: Long, textView: TypewriterTextView, text: String, charMode: Char, snippetId: Long) {
        countDownTimer = object : CountDownTimer(waitToHide, 100) {
            override fun onFinish() {
                b.controlLayout.transitionToEnd()
                b.readerTextSwitcher.setText(text)
                viewModel.snippetHandler.updateSnipDeltaStart(snippetId, System.currentTimeMillis())
                showShowCharsFab(charMode)
            }
            override fun onTick(millisUntilFinished: Long) {
                // maybe create a text view to countdown?
                // show tips on that space?

            }
        }.start()

    }

    private fun pickSnipTextToRead(snip: Snip) : String {
        return when (b.snippet!!.snippet.hideMode) {
            'S' -> { snip.textHidSingle }
            'D' -> { snip.textHidDouble }
            'T' -> { snip.textHidTriple }
            else -> snip.text
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
    private fun showShowCharsFab(hideMode: Char) {
        when (hideMode) {
            'S' -> { b.readerShowFullFab.show() }
            'D' -> { b.readerShowFullFab.show(); b.readerShowDoubleFab.show() }
            'T' -> { b.readerShowFullFab.show(); b.readerShowDoubleFab.show(); b.readerShowSingleFab.show()}
        }
    }
    private fun setUpShowFabsText(hideMode: Char, charsToHide: String) {
        when (hideMode) {
            'S' -> { b.readerShowFullFab.text = charsToHide.substring(0, 1) }
            'D' -> {
                b.readerShowFullFab.text = charsToHide.substring(0, 2)
                b.readerShowDoubleFab.text = charsToHide.substring(1, 2)
            }
            'T' -> {
                b.readerShowFullFab.text = charsToHide
                b.readerShowDoubleFab.text = charsToHide.substring(1)
                b.readerShowSingleFab.text = charsToHide.substring(2)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        speechRecognizer.destroy()
    }


    override fun onResume() {
        super.onResume()
//        hideSupportBar(true)
    }

    override fun onStop() {
        super.onStop()
        countDownTimer?.cancel()
        hideSupportBar(false)
    }
}