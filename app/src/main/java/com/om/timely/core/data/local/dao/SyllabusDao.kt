package com.om.timely.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.om.timely.core.data.local.entity.SyllabusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM syllabuses WHERE dept = :dept ORDER BY createdAt DESC, id DESC")
    fun getSyllabusesFlow(dept: String): Flow<List<SyllabusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabuses(syllabuses: List<SyllabusEntity>)

    @Query("DELETE FROM syllabuses WHERE dept = :dept")
    suspend fun clearSyllabusesByDept(dept: String)

    @Query("DELETE FROM syllabuses WHERE id = :id")
    suspend fun deleteById(id: Int)
}
