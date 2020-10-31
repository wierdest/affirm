package me.wierdest.affirmative.utilities

import java.util.*

/**
 * Collection of extension function to process texts
 */


/**
 * Takes a string in the format XXXX and returns XX-XX
 */
fun String.correctCEFRString() : String {
    return if (length <= 4) "${this.substring(0, 2)}-${this.substring(2, 4)}" else throw IllegalArgumentException("Wrong CEFR level string format")
}

/**
 * Translates a CEFR string to a more palatable English level version
 */

fun String.translateCEFRDescriptor() : String {
    return  when {
        this == "a1" -> { "starter"}
        this == "A1-A2" -> {"starter and pre-intermediate"}
        this == "A2" -> {"pre-intermediate"}
        this == "B1" -> {"intermediate"}
        this == "B1-B2" -> {"intermediate and upper-intermediate"}
        this == "B2" -> {"upper-intermediate"}
        this == "C1" -> {"advanced"}
        this == "C1-C2" -> {"advanced and proficient"}
        this == "C2" -> {"proficient"}
        else -> {
            throw IllegalArgumentException("String isn't a CEFR descriptor. Look: $this")
        }
    }
}


/**
 * Returns another string containing the 3 most common characters of the string
 */

fun String.getThreeMostCommonChars() : String {
    var result = ""
    val map = mutableMapOf<Char, Int>()
    prepareForComparison().groupingBy { it }.eachCountTo(map)
    for (i in 1..3) {
        val common = map.maxBy { it.value }
        result += common?.key
        map.remove(common?.key)
    }
    return result
}

/**
 * Makes a string lower case and removes punctuation and white space
 */

fun String.prepareForComparison() : String {
    return replace(Regex("[^\\w\\d]"), "").toLowerCase(Locale.getDefault())
}

/**
 * Returns the Levenshtein distance between this and target
 */

fun CharSequence.levenshtein(target: CharSequence) : Int {
    var dist = Array(length) { it }
    var newDist = Array(length) { 0 }

    for (i in 1 until target.length) {
        newDist[0] = i

        for (j in 1 until length) {
            val match = if (this[j - 1] == target [ i - 1]) 0 else 1

            val distReplace = dist[j - 1] + match
            val distInsert = dist[j] + 1
            val distDelete = newDist[j - 1] + 1

            newDist[j] = distInsert.coerceAtMost(distDelete).coerceAtMost(distReplace)
        }

        val swap = dist
        dist = newDist
        newDist = swap
    }
    return dist[length - 1]
}