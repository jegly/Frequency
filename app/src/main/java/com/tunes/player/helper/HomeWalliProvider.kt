package com.tunes.player.helper

import java.util.Calendar

object HomeWalliProvider {

    fun getTimeOfDay(): Day {
        val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hourOfDay in 5..11 -> Day.MORNING
            hourOfDay in 12..16 -> Day.AFTERNOON
            hourOfDay in 17..18 -> Day.EVENING
            else -> Day.NIGHT
        }
    }

    enum class Day {
        MORNING, AFTERNOON, EVENING, NIGHT
    }
}
