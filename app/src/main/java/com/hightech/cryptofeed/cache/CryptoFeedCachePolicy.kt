package com.hightech.cryptofeed.cache

import java.util.Calendar
import java.util.Date

class CryptoFeedCachePolicy constructor(
    private val currentDate: Date,
    private val calendar: Calendar = Calendar.getInstance()
) {
    private val maxCacheAgeInDays: Int = 1

    fun validate(timestamp: Date): Boolean {
        calendar.apply {
            time = timestamp
            add(Calendar.DAY_OF_YEAR, maxCacheAgeInDays)
        }
        val maxCacheAge = calendar.time
        return currentDate.before(maxCacheAge)
    }
}