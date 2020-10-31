package me.wierdest.affirmative.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.wierdest.affirmative.R
import me.wierdest.affirmative.utilities.*

class SnippetHandler(private val snippetDatabase: SnippetDAO, private val uiScope: CoroutineScope) {
    val TAG = javaClass.name

    val allSnippetsLive = snippetDatabase.getAllSnippetsLive()
    val allSnippetsWithSnipsLive = snippetDatabase.getAllSnippetWithSnipsLive()


    private val _snippetToRead = MutableLiveData<SnippetWithSnips>()
    val snippetToRead: LiveData<SnippetWithSnips> = _snippetToRead

    fun pickSnippetToReadByLevel(level: String) {
        uiScope.launch {
            _snippetToRead.value = withContext(Dispatchers.IO) {
                snippetDatabase.getLastSnippetWithSnipsByLevel(level)

            }
            Log.i(TAG, "Picked snippet to read: ${_snippetToRead.value?.snippet?.name}!")
        }
    }
    fun resetSnippetToRead() {
        uiScope.launch {
            _snippetToRead.value = null
        }
    }

    private val _readyToMakeSnips = MutableLiveData<Boolean>()
    val readyToMakeSnips: LiveData<Boolean> = _readyToMakeSnips
    fun resetReadyToMakeSnips() {
        uiScope.launch { _readyToMakeSnips.value = null }
    }
    fun createSnippetFromAssetFile(fileString: String, makeSnips: Boolean) {
        /**
         * This function makes it necessary for the files to be created in a fixed order. I guess that's ok for fixed and tested content.
         */
        uiScope.launch {
            _readyToMakeSnips.value = withContext(Dispatchers.IO) {
                val source = fileString.split(":")
                val new = buildSnippetFromAsset(source)
                snippetDatabase.insert(new)
                Log.i(TAG, "Built & Inserted snippet: ${new.level} / ${new.name}")
                Log.i(TAG, "This is snippet's raw: ${new.raw}")
                makeSnips
            }
        }
    }
    private fun buildSnippetFromAsset(source: List<String>) : Snippet {
        return Snippet().apply {
            name = source[1]
            isDialogue = source[3] == "true"
            level = source[5].prepareForComparison()
            icon = getSnippetIconId(source[7])
            topic = source[9]
            note = source[11]
            keywords = source[13]
            raw = source[15]
        }

    }

    private fun getSnippetIconId(id: String) : Int {
        return when (id.prepareForComparison()) {
            "world"-> {
                R.drawable.public_24px
            }
            "followthesign" -> {
                R.drawable.follow_the_signs_24px
            }
            "rowing" -> {
                R.drawable.rowing_24px
            }
            "hourglass" -> {
                R.drawable.hourglass_empty_24px
            }
            "history" -> {
                R.drawable.history_edu_24px
            }
            else ->
                throw Exception("UnknownIconId: $id")
        }
    }


    /**
     * Make snippet's snips
     */
    fun makeAllSnips() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val snippet = snippetDatabase.getAllSnippets()
                snippet.forEach {
                    // split using lookahead to keep the full stop delimiter attached to the substring
                    val sourceSnips = it?.raw?.split(Regex("(?<=[.])"))
                    if (sourceSnips != null) {
                        for ((index, snipText) in sourceSnips.withIndex()) {
                            if (snipText.isNotEmpty()) {
                                val charsToHide = snipText.getThreeMostCommonChars()
                                val single = snipText.replace(charsToHide[0], '_', true)
                                val double = single.replace(charsToHide[1], '_', true)
                                val triple = double.replace(charsToHide[2], '_', true)
                                snippetDatabase.insertSnip(
                                    Snip(
                                        snippetCreatorId = it.snippetId,
                                        snipIndex = index + 1,
                                        text = snipText,
                                        words = snipText.split(" ").size,
                                        charsToHide = charsToHide,
                                        textHidSingle = single,
                                        textHidDouble = double,
                                        textHidTriple = triple
                                    )
                                )

                            }
                        }
                    }
                }
                snippetDatabase.updateAll(*snippet)
                Log.i(TAG, "made all snips from assets")

            }
        }
    }

    /**
     * Add function below is more ideal to when the user manually adds a snippet - [ future ideal implementation ]
     */

    fun addSnippet(filename: String, source: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val testSnippet = snippetDatabase.getSnippetByRaw(source)
                if (testSnippet == null) {
                    val snippet = Snippet(raw = source, isDialogue = false, name = filename)
                    snippetDatabase.insert(snippet)
                } else {
                    Log.i(TAG, "We've already got a snippet with that source")
                }
            }
        }

    }

    fun updateSnipResults(id: Long, userSpoken: String, rmdb: Int, speechStartMilli: Long, endMilli: Long) {
        // updates the snip spoken and calculates the score, considering the levenshtein and the
        // time user took to read, considering its modifiers
        uiScope.launch {
            _snippetToRead.value = withContext(Dispatchers.IO) {
                val snippetToRead = snippetDatabase.getSnippetWithSnipsById(id)
                snippetToRead?.apply {
                    val currentSnip = snips[snippet.snipCount]
                    currentSnip.apply {
                        // to compare we prepare the strings, removing punctuation, whitespace and capital letters.
                        val base = text.prepareForComparison()
                        val toCompare = userSpoken.prepareForComparison()
                        levenshtein = toCompare.levenshtein(base)
                        deltaSpeechStart = speechStartMilli
                        deltaEnd = endMilli
                        loud = rmdb
                        delta = deltaEnd- deltaStart
                        wpm = words / (deltaEnd - deltaSpeechStart).toMinutes().toInt()
                        accuracy = levenshtein.linearConversion(
                            oldMax = 0,
                            oldMin =  if (base >= toCompare) base.length else toCompare.length,
                            newMax = 100,
                            newMin = 0
                        )
                        spoken = userSpoken
                    }
                    snippetDatabase.update(snippet)
                    Log.i(TAG, "Snippet ${snippet.snippetId}'s snip ${snippet.snipCount} spoken updated!")
                }
                snippetToRead
            }
        }
    }

    fun updateSnipCount(id: Long, decrease: Boolean) {
        uiScope.launch {
            _snippetToRead.value = withContext(Dispatchers.IO) {
                val snippetToRead = snippetDatabase.getSnippetWithSnipsById(id)
                snippetToRead?.apply {
                    if (snippet.snipCount <= snips.lastIndex) {
                        if (decrease) {
                            // if we decrease we need to remove the current spoken string
                            val currentSnipCount = snippet.snipCount
                            val currentSnip = snips[currentSnipCount]
                            currentSnip.spoken = ""
                            --snippet.snipCount
                        } else {
                            ++snippet.snipCount
                        }
                        snippetDatabase.update(snippet)
                        Log.i(TAG, "Updated snippet ${snippet.snippetId} snipCount to: ${snippet.snipCount}")
                    } else {
                        Log.i(TAG, "Didn't update snipCount, because it's more than the snips present")
                    }

                }
                snippetToRead
            }
        }
    }
    fun updateSnipDeltaStart(id: Long, milli: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                // maybe we don't need to update the toRead LiveData, right now
                val snippetToRead = snippetDatabase.getSnippetWithSnipsById(id)
                snippetToRead?.apply {
                    snips[snippet.snipCount].deltaStart = milli
                    snippetDatabase.update(snippet)
                    Log.i(TAG, "Snippet ${snippet.snippetId}'s snip ${snippet.snipCount} deltaStart updated to: $milli")
                }

            }
        }
    }
    fun updateSnipDeltaMod(id: Long, milli: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                // maybe we don't need to update the toRead LiveData, right now
                val snippetToRead = snippetDatabase.getSnippetWithSnipsById(id)
                snippetToRead?.apply {
                    snips[snippet.snipCount].deltaMod = milli
                    snippetDatabase.update(snippet)
                    Log.i(TAG, "Snippet ${snippet.snippetId}'s snip ${snippet.snipCount} deltaMod updated to: $milli")
                }
            }
        }
    }

}