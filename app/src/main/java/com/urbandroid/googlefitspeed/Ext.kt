package com.urbandroid.googlefitspeed

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

inline fun now() = System.currentTimeMillis()

fun Calendar.format(pattern: String = "yyyy-MM-dd HH:mm"): String = SimpleDateFormat(pattern).format(time)
val Long.calendar: Calendar
    get(): Calendar {
        val time = this
        return Calendar.getInstance().apply { timeInMillis = time }
    }
val Long.prettyDate: String get() = this.calendar.format("yyyy-MM-dd HH:mm")

const val LOG_TAG = "read-fit-sleep"
fun Any.info(msg: String) = Log.i(LOG_TAG, msg)
