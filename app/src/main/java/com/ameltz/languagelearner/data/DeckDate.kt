package com.ameltz.languagelearner.data

import java.time.Instant
import java.time.ZoneId

data class DeckDate(val deckDate: Instant) {
    val estTimeZone:ZoneId = ZoneId.of("America/New_York")
    fun getEpochMilli(): Long {
        return this.deckDate.atZone(estTimeZone).toLocalDate().atStartOfDay(estTimeZone).toInstant().toEpochMilli()
    }

    fun isTodayEST(): Boolean {
        val now = Instant.now().atZone(estTimeZone).toLocalDate()
        val deckDateInEST = this.deckDate.atZone(estTimeZone).toLocalDate()
        return now.equals(deckDateInEST)
    }
  companion object {
      fun getToday(): DeckDate {
          return DeckDate(Instant.now())
      }
  }
}
