package com.ameltz.languagelearner.data

import java.time.Instant
import java.time.ZoneId

data class DeckDate(val deckDate: Instant) {

    fun getEpochMilli(): Long {
        return this.deckDate.toEpochMilli()
    }

    fun isTodayEST(): Boolean {
        val now = Instant.now().atZone(ZoneId.of("America/New_York")).toLocalDate()
        val deckDateInEST = this.deckDate.atZone(ZoneId.of("America/New_York")).toLocalDate()
        return now.equals(deckDateInEST)
    }
  companion object {
      fun getToday(): DeckDate {
          return DeckDate(Instant.now())
      }
  }
}
