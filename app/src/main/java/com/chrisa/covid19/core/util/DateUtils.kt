package com.chrisa.covid19.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private const val RFC_1123_DATE_TIME = "EEE, dd MMM yyyy HH:mm:ss z"

    fun Date.addHours(hours: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = this
        calendar.add(Calendar.HOUR_OF_DAY, hours)

        return calendar.time
    }

    fun Date.toGmtDate(): String {
        val formatter = SimpleDateFormat(
            RFC_1123_DATE_TIME,
            Locale.UK
        )
        formatter.timeZone = TimeZone.getTimeZone("GMT")

        return formatter.format(this)
    }
}
