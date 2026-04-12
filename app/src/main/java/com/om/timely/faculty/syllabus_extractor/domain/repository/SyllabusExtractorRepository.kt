package com.om.timely.faculty.syllabus_extractor.domain.repository

import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.syllabus_extractor.domain.error.SyllabusProcessingError
import com.om.timely.faculty.syllabus_extractor.domain.model.SubjectData
import kotlinx.coroutines.flow.Flow
import com.om.timely.faculty.syllabus_extractor.domain.model.SemesterData
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusDraft
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusJobUpdate

interface SyllabusExtractorRepository {
    suspend fun startExtractionJob(imageUrls: List<String>): Result<String, SyllabusProcessingError>
    fun observeExtractionJob(jobId: String): Flow<SyllabusJobUpdate>

    suspend fun confirmAndSaveSubjects(
        name: String,
        dept: String,
        termType: String,
        semesters: List<SemesterData>
    ): Result<Unit, SyllabusProcessingError>

    suspend fun saveDraft(
        id: Int? = null,
        name: String,
        dept: String,
        termType: String,
        semesters: List<SemesterData>
    ): Result<Unit, SyllabusProcessingError>

    fun getAllDrafts(): Flow<List<SyllabusDraft>>
    
    suspend fun deleteDraft(id: Int): Result<Unit, SyllabusProcessingError>
    
    suspend fun getDraftById(id: Int): SyllabusDraft?
}
