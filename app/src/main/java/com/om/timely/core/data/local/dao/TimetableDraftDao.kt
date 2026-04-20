package com.om.timely.core.data.local.dao

import androidx.room.*
import com.om.timely.core.data.local.entity.TimetableDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDraftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: TimetableDraftEntity): Long

    @Query("SELECT * FROM timetable_drafts ORDER BY savedAt DESC")
    fun getAllDraftsFlow(): Flow<List<TimetableDraftEntity>>

    @Query("SELECT * FROM timetable_drafts WHERE id = :id")
    suspend fun getDraftById(id: Int): TimetableDraftEntity?

    @Delete
    suspend fun deleteDraft(draft: TimetableDraftEntity)

    @Query("DELETE FROM timetable_drafts WHERE id = :id")
    suspend fun deleteDraftById(id: Int)
}
