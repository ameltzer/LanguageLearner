package com.ameltz.languagelearner.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add new columns to CardInDeck table
        db.execSQL("ALTER TABLE CardInDeck ADD COLUMN easyCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE CardInDeck ADD COLUMN mediumCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE CardInDeck ADD COLUMN hardCount INTEGER NOT NULL DEFAULT 0")

        // Rename daysToNextShow to priority
        // SQLite doesn't support column renaming directly, so we need to:
        // 1. Create a new table with the new schema
        db.execSQL("""
            CREATE TABLE CardInDeck_new (
                uuid BLOB NOT NULL PRIMARY KEY,
                priority INTEGER NOT NULL,
                easyCount INTEGER NOT NULL,
                mediumCount INTEGER NOT NULL,
                hardCount INTEGER NOT NULL,
                cardId BLOB NOT NULL,
                deckId BLOB NOT NULL,
                FOREIGN KEY(cardId) REFERENCES Card(uuid) ON DELETE CASCADE,
                FOREIGN KEY(deckId) REFERENCES Deck(uuid) ON DELETE CASCADE
            )
        """)

        // 2. Create indexes on the new table
        db.execSQL("CREATE UNIQUE INDEX index_CardInDeck_new_cardId_deckId ON CardInDeck_new(cardId, deckId)")

        // 3. Copy data from old table to new table
        // Set priority = 1 for all existing cards (treat them all as high priority)
        db.execSQL("""
            INSERT INTO CardInDeck_new (uuid, priority, easyCount, mediumCount, hardCount, cardId, deckId)
            SELECT uuid, 1, easyCount, mediumCount, hardCount, cardId, deckId
            FROM CardInDeck
        """)

        // 4. Drop old table
        db.execSQL("DROP TABLE CardInDeck")

        // 5. Rename new table to original name
        db.execSQL("ALTER TABLE CardInDeck_new RENAME TO CardInDeck")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add lastReviewDate column to CardInDeck table
        db.execSQL("ALTER TABLE CardInDeck ADD COLUMN lastReviewDate INTEGER")

        // Recalculate priority for all existing cards using new formula
        // New cards (no reviews) get priority 100
        db.execSQL("""
            UPDATE CardInDeck
            SET priority = CASE
                WHEN (easyCount + mediumCount + hardCount) = 0 THEN 100
                ELSE MAX(1, CAST(((hardCount * 3.0 + mediumCount) / (easyCount + mediumCount + hardCount)) * 100 AS INTEGER))
            END
        """)
    }
}
