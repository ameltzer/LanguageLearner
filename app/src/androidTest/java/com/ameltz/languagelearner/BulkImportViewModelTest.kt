package com.ameltz.languagelearner

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ameltz.languagelearner.data.AppDatabase
import com.ameltz.languagelearner.data.entity.Card
import com.ameltz.languagelearner.data.repository.DefaultRepository
import com.ameltz.languagelearner.ui.viewmodel.BulkImportViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BulkImportViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DefaultRepository
    private lateinit var viewModel: BulkImportViewModel

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DefaultRepository(
            db.deckDAO(),
            db.cardDAO(),
            db.cardInDeckDao(),
            db.studyDeckDao(),
            db.studyCardDao(),
            db.settingDao()
        )
        viewModel = BulkImportViewModel(repository)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun importDeckWithDuplicateCard_doesNotCreateDuplicate() {
        // Pre-insert a card with the same front/back that will be in the import
        val existingCard = Card.createCard("hello", "world")
        repository.upsertCard(existingCard)

        // Import a deck containing a card with the same front/back
        val importContent = "# TestDeck\nhello\tworld"
        viewModel.importAllDecks(importContent)

        // Only one card should exist — no duplicate was created
        val allCards = repository.getAllCards()
        assertEquals(1, allCards.size)
        assertEquals("hello", allCards[0].front)
        assertEquals("world", allCards[0].back)
    }
}
