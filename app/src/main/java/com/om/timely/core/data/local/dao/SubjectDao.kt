package com.om.timely.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.om.timely.core.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects WHERE syllabusId = :syllabusId")
    fun getSubjectsBySyllabusFlow(syllabusId: Int): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE department = :dept")
    fun getAllSubjectsByDeptFlow(dept: String): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubject(subjectId: Int)
    
    @Query("DELETE FROM subjects WHERE syllabusId = :syllabusId")
    suspend fun clearSubjectsBySyllabus(syllabusId: Int)
}
