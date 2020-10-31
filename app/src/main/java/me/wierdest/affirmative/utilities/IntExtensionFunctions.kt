package me.wierdest.affirmative.utilities

/**
 * Simple linear conversion: converts a value within an old range
 * to a new value within a new range, and returns it
 */
fun Int.linearConversion(oldMax: Int, oldMin: Int, newMax: Int, newMin: Int) : Int {
    val oldRange = oldMax - oldMin
    return if (oldRange == 0) {
        newMin
    } else {
        val newRange = newMax - newMin
        (((this - oldMin) * newRange) / oldRange) + newMin
    }
}