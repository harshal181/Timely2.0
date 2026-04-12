package com.om.timely.core.data.local.dao

import androidx.room.*
import com.om.timely.core.data.local.entity.SyllabusDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: SyllabusDraftEntity)

    @Query("SELECT * FROM syllabus_drafts ORDER BY createdAt DESC")
    fun getAllDraftsFlow(): Flow<List<SyllabusDraftEntity>>

    @Query("SELECT * FROM syllabus_drafts WHERE id = :id")
    suspend fun getDraftById(id: Int): SyllabusDraftEntity?

    @Query("DELETE FROM syllabus_drafts WHERE id = :id")
    suspend fun deleteDraftById(id: Int)
}
