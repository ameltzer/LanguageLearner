package com.ameltz.languagelearner.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ameltz.languagelearner.ui.model.StudyCardOfTheDay
import com.ameltz.languagelearner.ui.model.StudyDeckOfTheDay
import java.time.Instant
import kotlin.uuid.Uuid

@Entity(
    indices=[Index(value = ["deckId"], unique = true), Index(value=["date"])],
    foreignKeys = [ForeignKey(
        entity = Deck::class,
        parentColumns = ["uuid"],
        childColumns = ["deckId"],
    )]
)
data class StudyDeck(@PrimaryKey val uuid: Uuid, val deckId: Uuid,
                     val completed: Boolean, val date: Long)

data class StudyDeckWithCards(
    @Embedded val studyDeck: StudyDeck,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "studyDeck",
        entity = StudyCard::class
    )
    val cards: List<StudyCardWithCard>
) {
    fun toStudyDeckOfTheDay(): StudyDeckOfTheDay {
        return StudyDeckOfTheDay(studyDeck.uuid, studyDeck.deckId,
            cards.map { it.toInitialStudyCard() },
            studyDeck.completed,
            Instant.ofEpochMilli(studyDeck.date))
    }
}

@Entity(
    foreignKeys = [ForeignKey(
        entity = CardInDeck::class,
        parentColumns = ["uuid"],
        childColumns = ["cardInDeckId"]
    )]
)
data class StudyCard(@PrimaryKey val uuid: Uuid, val cardInDeckId:Uuid, val nextShowDays: Int, val learned: Boolean,
                     val studyDeck: Uuid)

data class StudyCardWithCard(
    @Embedded val studyCardOfTheDay: StudyCard,
    @Relation(
        parentColumn = "cardInDeckId",
        entityColumn = "uuid",
        entity = CardInDeck::class
    )
    val cardInDeck: CardInDeckWithCard
) {
    fun toInitialStudyCard(): StudyCardOfTheDay {
        return StudyCardOfTheDay(cardInDeck.card.front, cardInDeck.card.back, studyCardOfTheDay.nextShowDays,
            studyCardOfTheDay.learned, 0, cardInDeck.cardInDeck.deckId, studyCardOfTheDay.uuid)
    }
}