package me.racci.bloodnight.util

import java.util.*

// Proudly stolen from https://github.com/sighrobot/Moon/blob/master/src/com/abe/moon/MoonPhase.java
object MoonPhase {
    /**
     * Computes the moon phase index as a value from 0 to 7 Used to display the phase name and the moon image for the
     * current phase.
     *
     *
     * 0 is a New Moon. 4 is a Full Moon
     *
     * @param cal Calendar calendar object for today's date
     * @return moon index 0..7
     */
    fun computePhaseIndex(cal: Calendar): Int {
        val year = cal.get(Calendar.YEAR)
        var month = cal.get(Calendar.MONTH) + 1 // 0 = Jan, 1 = Feb, etc.
        val day = cal.get(Calendar.DATE)
        val hour = cal.get(Calendar.HOUR)
        val min = cal.get(Calendar.MINUTE)
        val sec = cal.get(Calendar.SECOND)
        val dayExact = day + hour / 24.0 + min / 1440.0 + sec / 86400.0
        if (month > 12) {
            month = 0
        }
        val dayYear = intArrayOf(
            -1, -1, 30, 58, 89, 119,
            150, 180, 211, 241, 272,
            303, 333
        )
        // Day in the year
        var diy = dayExact + dayYear[month] // Day in the year
        if (month > 2 && isLeapYearP(year)) {
            diy++
        } // Leapyear fixup
        // Century number (1979 = 20)
        val cent = year / 100 + 1 // Century number
        // Moon's golden number
        val golden = year % 19 + 1 // Golden number
        // Age of the moon on Jan. 1
        var epact = ((11 * golden + 20 // Golden number
                + (8 * cent + 5) / 25) - 5 // 400 year cycle
                - (3 * cent / 4 - 12)) % 30 //Leap year correction
        if (epact <= 0) {
            epact += 30
        } // Age range is 1 until 30
        if (epact == 25 && golden > 11 ||
            epact == 24
        ) {
            epact++

            // Calculate the phase, using the magic numbers defined above.
            // Note that (phase and 7) is equivalent to (phase mod 8) and
            // is needed on two days per year (when the algorithm yields 8).
        }

        // Calculate the phase, using the magic numbers defined above.
        // Note that (phase and 7) is equivalent to (phase mod 8) and
        // is needed on two days per year (when the algorithm yields 8).
        // this.factor = ((((diy + (double)epact) * 6) + 11) % 100 );
        // Moon phase
        return ((diy.toInt() + epact) * 6 + 11) % 177 / 22 and 7
    }

    /**
     * isLeapYearP Return true if the year is a leapyear
     */
    private fun isLeapYearP(year: Int): Boolean {
        return year % 4 == 0 &&
                (year % 400 == 0 || year % 100 != 0)
    }
}