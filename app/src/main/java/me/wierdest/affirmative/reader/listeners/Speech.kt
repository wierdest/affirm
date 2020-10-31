package me.wierdest.affirmative.reader.listeners

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import me.wierdest.affirmative.databinding.FragmentReaderBinding

class Speech (private val b: FragmentReaderBinding) : RecognitionListener {
    val TAG = javaClass.name
    private var speechStart: Long = 0L
    private var speechEnd: Long = 0L
    private var loudest: Int = 0

    override fun onReadyForSpeech(params: Bundle?) {
        // show reading progress bar and set progress as indeterminate
        b.readProgressBar.apply {
            visibility = View.VISIBLE
            isIndeterminate = true

        }
    }

    override fun onRmsChanged(rmsdB: Float) {
        b.readProgressBar.progress = rmsdB.toInt()
        val loudness = rmsdB.toInt()
        if (loudness > loudest) {
            loudest = loudness
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {
    }

    override fun onPartialResults(partialResults: Bundle?) {
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
    }

    override fun onBeginningOfSpeech() {
        // set reading progress bar max progress and not indeterminate
        b.readProgressBar.apply {
            isIndeterminate = false
            max = 10
        }
        speechStart = System.currentTimeMillis()
    }

    override fun onEndOfSpeech() {
        b.readProgressBar.apply {
            isIndeterminate = true
            visibility = View.INVISIBLE
        }
        speechEnd = System.currentTimeMillis()

    }

    override fun onError(error: Int) {
        Log.i(TAG, "speech error: ${getErrorString(error)}")

    }

    override fun onResults(results: Bundle?) {
        val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
            it[0]
        }
        Log.i(TAG, "spoken text: $spokenText")
        // update the snip's spoken:
        spokenText?.let {
            val snippetId = b.myViewModel!!.snippetHandler.snippetToRead.value!!.snippet.snippetId
            b.myViewModel!!.snippetHandler.updateSnipResults(snippetId, it, loudest, speechStart, speechEnd)
        }
    }

    private fun getErrorString(code: Int) : String {
        return when (code) {
            SpeechRecognizer.ERROR_AUDIO -> {
                "Audio recording error"
            }
            SpeechRecognizer.ERROR_CLIENT -> {
                "Client side error"
            }
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                "Insufficient permissions error"
            }
            SpeechRecognizer.ERROR_NETWORK -> {
                "Network error"
            }
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                "Network operation timed out"
            }
            SpeechRecognizer.ERROR_NO_MATCH -> {
                "No recognition result matched"
            }
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                "RecognitionService is busy"
            }
            SpeechRecognizer.ERROR_SERVER -> {
                "Server sends error status"
            }
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                "No speech input"
            }
            else -> "Something went weirdly wrong!"
        }
    }

}