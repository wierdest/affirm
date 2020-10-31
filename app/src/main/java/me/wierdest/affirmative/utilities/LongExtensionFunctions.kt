package me.wierdest.affirmative.utilities

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

/**
 * Converts milliseconds to minutes
 */

fun Long.toMinutes() : Long {
    return ((this / (1000 * 60)) % 60)
}

/**
 * Takes the Long for the time in milliseconds and
 * converts it to a nicely formatted string for display.
 *
 * EEEE - Display the long letter version of the weekday
 * MMM - Display the letter abbreviation of the month
 * dd-yyyy - day in month and full year numerically
 * HH:mm - Hours and minutes in 24hr format
 */
@SuppressLint("SimpleDateFormat")
fun Long.convertLongToDateString(): String {
    return SimpleDateFormat("EEEE MMM-dd-yy' at: 'HH:mm")
        .format(this).toString()
}