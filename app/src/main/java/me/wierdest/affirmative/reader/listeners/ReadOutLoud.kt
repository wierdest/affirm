package me.wierdest.affirmative.reader.listeners

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import me.wierdest.affirmative.databinding.FragmentReaderBinding

class ReadOutLoud(private val b: FragmentReaderBinding, private val speechRecognizer: SpeechRecognizer) : View.OnClickListener {
    override fun onClick(v: View?) {
        b.readNextFab.visibility = View.INVISIBLE
        b.readPreviousFab.visibility = View.INVISIBLE
        // start listening intent
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, `package`)
        }
        speechRecognizer.startListening(intent)

    }

}