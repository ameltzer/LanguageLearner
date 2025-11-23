package com.ameltz.languagelearner.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ameltz.languagelearner.data.entity.Setting

@Dao
interface SettingDao {
    @Query("SELECT * FROM setting WHERE `key` = :key")
    fun getSetting(key: String): Setting?

    @Query("SELECT * FROM setting")
    fun getAllSettings(): List<Setting>

    @Upsert
    fun upsertSetting(setting: Setting)
}
