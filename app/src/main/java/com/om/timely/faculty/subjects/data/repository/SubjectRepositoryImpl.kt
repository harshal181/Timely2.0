package com.om.timely.faculty.subjects.data.repository

import com.om.timely.BuildConfig
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.faculty.subjects.data.dto.SubjectDto
import com.om.timely.faculty.subjects.data.mappers.toCreateDto
import com.om.timely.faculty.subjects.data.mappers.toDomain
import com.om.timely.faculty.subjects.data.mappers.toUpdateDto
import com.om.timely.faculty.subjects.data.dto.SyllabusDto
import com.om.timely.faculty.subjects.domain.error.SubjectError
import com.om.timely.faculty.subjects.domain.model.Subject
import com.om.timely.faculty.subjects.domain.model.Syllabus
import com.om.timely.faculty.subjects.domain.repository.SubjectRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.om.timely.core.data.local.dao.SubjectDao
import com.om.timely.core.data.local.dao.SyllabusDao
import com.om.timely.faculty.subjects.data.mappers.toEntity

class SubjectRepositoryImpl(
    private val httpClient: HttpClient,
    private val subjectDao: SubjectDao,
    private val syllabusDao: SyllabusDao
) : SubjectRepository {

    override fun getSyllabuses(dept: String): Flow<List<Syllabus>> {
        return syllabusDao.getSyllabusesFlow(dept).map { entityList ->
            entityList.map { it.toDomain() }
        }
    }

    override suspend fun fetchSyllabuses(dept: String): Result<Unit, SubjectError> {
        return safeCall<List<SyllabusDto>> {
            httpClient.get("${BuildConfig.BASE_URL}/api/subjects/syllabuses") {
                parameter("dept", dept)
            }
        }.map { dtoList ->
            val entities = dtoList.map { it.toDomain().toEntity() }
            syllabusDao.clearSyllabusesByDept(dept)
            syllabusDao.insertSyllabuses(entities)
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> SubjectError.Local.SUBJECT_NOT_FOUND
                else -> SubjectError.Network(error)
            }
        }
    }

    override fun getSubjects(syllabusId: Int): Flow<List<Subject>> {
        return subjectDao.getSubjectsBySyllabusFlow(syllabusId).map { entityList ->
            entityList.map { it.toDomain() }
        }
    }

    override suspend fun fetchSubjects(syllabusId: Int): Result<Unit, SubjectError> {
        return safeCall<List<SubjectDto>> {
            httpClient.get("${BuildConfig.BASE_URL}/api/subjects/") {
                parameter("syllabus_id", syllabusId)
            }
        }.map { dtoList ->
            val entities = dtoList.map { it.toDomain().toEntity() }
            subjectDao.clearSubjectsBySyllabus(syllabusId)
            subjectDao.insertSubjects(entities)
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> SubjectError.Local.SUBJECT_NOT_FOUND
                else -> SubjectError.Network(error)
            }
        }
    }

    override fun getAllSubjects(): Flow<List<Subject>> {
        return subjectDao.getAllSubjectsFlow().map { entityList ->
            entityList.map { it.toDomain() }
        }
    }

    override suspend fun fetchAllSubjects(): Result<Unit, SubjectError> {
        return safeCall<List<SubjectDto>> {
            httpClient.get("${BuildConfig.BASE_URL}/api/subjects/all")
        }.map { dtoList ->
            val entities = dtoList.map { it.toDomain().toEntity() }
            
            // We just REPLACE into the database for existing subjects.
            // If we want to clear subjects for dept, we'd need another DAO method, but REPLACE updates existing ones.
            subjectDao.insertSubjects(entities)
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> SubjectError.Local.SUBJECT_NOT_FOUND
                else -> SubjectError.Network(error)
            }
        }
    }

    override suspend fun addSubject(subject: Subject): Result<Unit, SubjectError> {
        return safeCall<SubjectDto> { // Assuming backend returns the created subject, but we just want Unit success
            httpClient.post("${BuildConfig.BASE_URL}/api/subjects/") {
                contentType(ContentType.Application.Json)
                setBody(subject.toCreateDto())
            }
        }.map { dtoList ->
            // Assume the backend returns the updated subject Dto, we map it to Entity and insert
            // (Even if it just returns success, since it's safeCall<SubjectDto>, we have the dto)
            val entity = dtoList.toDomain().toEntity()
            subjectDao.insertSubject(entity)
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.CONFLICT -> SubjectError.Local.CONFLICT
                else -> SubjectError.Network(error)
            }
        }
    }

    override suspend fun updateSubject(subject: Subject): Result<Unit, SubjectError> {
        return safeCall<SubjectDto> {
            httpClient.patch("${BuildConfig.BASE_URL}/api/subjects/${subject.id}") {
                contentType(ContentType.Application.Json)
                setBody(subject.toUpdateDto())
            }
        }.map { dtoList ->
            val entity = dtoList.toDomain().toEntity()
            subjectDao.insertSubject(entity)
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> SubjectError.Local.SUBJECT_NOT_FOUND
                NetworkError.BAD_REQUEST -> SubjectError.Local.CONFLICT
                else -> SubjectError.Network(error)
            }
        }
    }

    override suspend fun deleteSubject(subjectId: Int): Result<Unit, SubjectError> {
        return safeCall<Unit> {
            httpClient.delete("${BuildConfig.BASE_URL}/api/subjects/${subjectId}")
        }.map {
            subjectDao.deleteSubject(subjectId)
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> SubjectError.Local.SUBJECT_NOT_FOUND
                else -> SubjectError.Network(error)
            }
        }
    }

    override suspend fun deleteSyllabus(syllabusId: Int): Result<Unit, SubjectError> {
        return safeCall<Unit> {
            httpClient.delete("${BuildConfig.BASE_URL}/api/syllabus/${syllabusId}")
        }.map {
            syllabusDao.deleteById(syllabusId)
            subjectDao.clearSubjectsBySyllabus(syllabusId)
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> SubjectError.Local.SUBJECT_NOT_FOUND
                else -> SubjectError.Network(error)
            }
        }
    }
}
