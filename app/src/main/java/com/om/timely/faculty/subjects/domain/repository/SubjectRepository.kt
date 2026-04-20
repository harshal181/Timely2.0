package com.om.timely.faculty.subjects.domain.repository

import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.subjects.domain.error.SubjectError
import com.om.timely.faculty.subjects.domain.model.Subject
import com.om.timely.faculty.subjects.domain.model.Syllabus

import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun getSyllabuses(dept: String): Flow<List<Syllabus>>
    suspend fun fetchSyllabuses(dept: String): Result<Unit, SubjectError>

    fun getSubjects(syllabusId: Int): Flow<List<Subject>>
    suspend fun fetchSubjects(syllabusId: Int): Result<Unit, SubjectError>
    
    fun getAllSubjects(): Flow<List<Subject>>
    suspend fun fetchAllSubjects(): Result<Unit, SubjectError>
    suspend fun addSubject(subject: Subject): Result<Unit, SubjectError>
    suspend fun updateSubject(subject: Subject): Result<Unit, SubjectError>
    suspend fun deleteSubject(subjectId: Int): Result<Unit, SubjectError>
    suspend fun deleteSyllabus(syllabusId: Int): Result<Unit, SubjectError>
}
