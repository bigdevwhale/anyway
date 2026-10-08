package io.cyberdise.anyway

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

object Life {
    fun end(birth: LocalDate, years: Int): LocalDate = birth.plusYears(years.toLong())

    /** Saturdays from today (inclusive) until the expected end (exclusive). */
    fun saturdaysLeft(birth: LocalDate, years: Int, today: LocalDate): Long {
        val end = end(birth, years)
        val firstSaturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
        if (!firstSaturday.isBefore(end)) return 0
        return (ChronoUnit.DAYS.between(firstSaturday, end) - 1) / 7 + 1
    }

    fun fractionLived(birth: LocalDate, years: Int, today: LocalDate): Float {
        val total = ChronoUnit.DAYS.between(birth, end(birth, years)).toFloat()
        val lived = ChronoUnit.DAYS.between(birth, today).toFloat()
        return (lived / total).coerceIn(0f, 1f)
    }

    /** A stable pick for the day, so the screen doesn't change on every open. */
    fun <T> ofTheDay(items: Array<T>, today: LocalDate, salt: Int = 0): T =
        items[Math.floorMod(today.toEpochDay() + salt, items.size.toLong()).toInt()]
}
