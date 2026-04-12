package com.om.timely.faculty.syllabus_extractor.data.repository

import android.content.Context
import android.net.Uri
import com.om.timely.BuildConfig
import com.om.timely.core.data.local.dao.SyllabusDraftDao
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.faculty.syllabus_extractor.data.dto.ExtractedSubjectDto
import com.om.timely.faculty.syllabus_extractor.data.dto.SyllabusBulkImportRequestDto
import com.om.timely.faculty.syllabus_extractor.data.dto.SyllabusBulkImportSemesterDto
import com.om.timely.faculty.syllabus_extractor.data.dto.SyllabusBulkImportSubjectDto
import com.om.timely.faculty.syllabus_extractor.data.dto.SyllabusExtractionRequestDto
import com.om.timely.faculty.syllabus_extractor.data.dto.SyllabusJobCreateResponseDto
import com.om.timely.faculty.syllabus_extractor.data.dto.SyllabusJobResponseDto
import com.om.timely.faculty.syllabus_extractor.domain.error.SyllabusProcessingError
import com.om.timely.faculty.syllabus_extractor.domain.model.SubjectData
import com.om.timely.faculty.syllabus_extractor.domain.model.SemesterData
import com.om.timely.faculty.syllabus_extractor.domain.model.SubjectExtractionData
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusDraft
import com.om.timely.faculty.syllabus_extractor.domain.repository.SyllabusExtractorRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusJobUpdate
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import java.util.UUID

class SyllabusExtractorRepositoryImpl(
    private val httpClient: HttpClient,
    private val syllabusDraftDao: SyllabusDraftDao
) : SyllabusExtractorRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun startExtractionJob(imageUrls: List<String>): Result<String, SyllabusProcessingError> {
        return try {
            val response = httpClient.post("${BuildConfig.BASE_URL}/api/syllabus/jobs") {
                contentType(ContentType.Application.Json)
                setBody(SyllabusExtractionRequestDto(imageUrls = imageUrls))
            }
            if (response.status.isSuccess()) {
                val dto = response.body<SyllabusJobCreateResponseDto>()
                Result.Success(dto.jobId)
            } else {
                Result.Error(SyllabusProcessingError.ServerRejection("Server rejected job creation: ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(SyllabusProcessingError.Network(NetworkError.NOT_FOUND))
        }
    }

    override fun observeExtractionJob(jobId: String): Flow<SyllabusJobUpdate> = flow {
        try {
            httpClient.sse("${BuildConfig.BASE_URL}/api/syllabus/jobs/$jobId/stream") {
                incoming.collect { event ->
                    val data = event.data
                    if (data != null) {
                        try {
                                val jsonConfig = Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }
                            val dto = jsonConfig.decodeFromString<SyllabusJobResponseDto>(data)
                            when (dto.status) {
                                "pending" -> emit(SyllabusJobUpdate.Progress("pending", dto.error ?: "Job is initializing..."))
                                "processing" -> emit(SyllabusJobUpdate.Progress("processing", dto.error ?: "AI is extracting syllabus data..."))
                                "parsing" -> emit(SyllabusJobUpdate.Progress("parsing", dto.error ?: "Structuring extracted data..."))
                                "completed" -> {
                                    val parsedData = dto.parsedData
                                    if (parsedData != null && parsedData.semesters.isNotEmpty()) {
                                        val domainSemesters = parsedData.semesters.map { semDto ->
                                            SemesterData(
                                                semester = semDto.semester,
                                                subjects = semDto.subjects.map { subjDto ->
                                                    SubjectData(
                                                        id = UUID.randomUUID().toString(),
                                                        name = subjDto.name,
                                                        courseCode = subjDto.courseCode,
                                                        lecture = subjDto.lecture,
                                                        tutorial = subjDto.tutorial,
                                                        practical = subjDto.practical,
                                                        type = subjDto.type,
                                                        electiveGroup = subjDto.electiveGroup
                                                    )
                                                }
                                            )
                                        }
                                        emit(SyllabusJobUpdate.Completed(SubjectExtractionData(semesters = domainSemesters)))
                                    } else {
                                        emit(SyllabusJobUpdate.Failed(SyllabusProcessingError.ParsingError("Completed but no parsed data found in job response.")))
                                    }
                                }
                                "failed" -> emit(SyllabusJobUpdate.Failed(SyllabusProcessingError.ServerRejection(dto.error ?: "Job Failed")))
                            }
                        } catch (e: Exception) {
                            // Ignored: possibly an empty keep-alive or malformed JSON
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(SyllabusJobUpdate.Failed(SyllabusProcessingError.NetworkFailure))
        }
    }

    override suspend fun confirmAndSaveSubjects(
        name: String,
        dept: String,
        termType: String,
        semesters: List<SemesterData>
    ): Result<Unit, SyllabusProcessingError> {
        val response = safeCall<Unit> {
            httpClient.post("${BuildConfig.BASE_URL}/api/subjects/bulk-import") {
                contentType(ContentType.Application.Json)
                val dtos = semesters.map { sem ->
                    SyllabusBulkImportSemesterDto(
                        semester = sem.semester,
                        subjects = sem.subjects.map { subj ->
                            SyllabusBulkImportSubjectDto(
                                name = subj.name,
                                courseCode = subj.courseCode?.takeIf { it.isNotBlank() },
                                lecture = subj.lecture,
                                tutorial = subj.tutorial,
                                practical = subj.practical,
                                type = subj.type,
                                electiveGroup = subj.electiveGroup.takeIf { !it.isNullOrBlank() }
                            )
                        }
                    )
                }
                val requestBody = SyllabusBulkImportRequestDto(
                    name = name,
                    dept = dept,
                    termType = termType,
                    subjectData = dtos
                )
                setBody(requestBody)
            }
        }
        return response.map {
        }.mapError { error ->
            SyllabusProcessingError.Network(error)
        }
    }

    override suspend fun saveDraft(
        id: Int?,
        name: String,
        dept: String,
        termType: String,
        semesters: List<SemesterData>
    ): Result<Unit, SyllabusProcessingError> {
        return try {
            val entity = com.om.timely.core.data.local.entity.SyllabusDraftEntity(
                id = id ?: 0,
                name = name,
                dept = dept,
                termType = termType,
                dataJson = json.encodeToString(semesters)
            )
            syllabusDraftDao.insertDraft(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(SyllabusProcessingError.ParsingError("Failed to save draft locally"))
        }
    }

    override fun getAllDrafts(): Flow<List<SyllabusDraft>> {
        return syllabusDraftDao.getAllDraftsFlow().map { entities ->
            entities.map { entity ->
                SyllabusDraft(
                    id = entity.id,
                    name = entity.name,
                    dept = entity.dept,
                    termType = entity.termType,
                    semesters = json.decodeFromString(entity.dataJson),
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun deleteDraft(id: Int): Result<Unit, SyllabusProcessingError> {
        return try {
            syllabusDraftDao.deleteDraftById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(SyllabusProcessingError.ParsingError("Failed to delete draft"))
        }
    }

    override suspend fun getDraftById(id: Int): SyllabusDraft? {
        return syllabusDraftDao.getDraftById(id)?.let { entity ->
            SyllabusDraft(
                id = entity.id,
                name = entity.name,
                dept = entity.dept,
                termType = entity.termType,
                semesters = json.decodeFromString(entity.dataJson),
                createdAt = entity.createdAt
            )
        }
    }

    // private fun readUriBytes(uri: Uri)... removed since we use URLs now
}
