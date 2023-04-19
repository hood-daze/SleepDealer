package com.hood.sleepdealer.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDateTime

object LocalDateTimeConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toDate(dateString: String?): LocalDateTime? {
        return if (dateString == null) {
            null
        } else {
            LocalDateTime.parse(dateString)
        }
    }

    @TypeConverter
    fun toDateString(date: LocalDateTime?): String? {
        return date?.toString()
    }
}