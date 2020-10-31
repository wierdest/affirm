package me.wierdest.affirmative.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snip_table")
data class Snip(
    @PrimaryKey(autoGenerate = true)
    var snipId: Long = 0L,
    val snippetCreatorId: Long,
    val snipIndex: Int, // index of string in the snippet text
    var text: String = "", // original text
    var spoken: String = "", // spoken text
    var charsToHide: String, // top three most common chars to hide
    var textHidSingle: String, // text without the one most common char
    var textHidDouble: String, // text without the two most common chars
    var textHidTriple: String, // text without the three most common chars

    var levenshtein: Int = 0,
    var words: Int = 0, // number of words
    var wpm: Int = 0, // words per minute: words / (deltaEnd - deltaSpeechStart)
    var loud: Int = 0,
    var deltaStart: Long = 0L, // time counter start from the moment the text is completely rendered to be read
    var deltaSpeechStart: Long = 0L,
    var deltaEnd: Long = 0L, // time counter end
    var delta: Long = 0L, // time diff from the moment we show the snip, to the moment the user finishes inserting spoken text
    var deltaMod: Long = 0L, // time counter for when the user hits a showChar button, it should be positive or negative
    var accuracy: Int = 0, // this is a linear conversion based on the levenshtein distance and the raw length of the text
    var confusion: String = "" // a string in the format: "confusedword_vs_word", with elements separated by space
)