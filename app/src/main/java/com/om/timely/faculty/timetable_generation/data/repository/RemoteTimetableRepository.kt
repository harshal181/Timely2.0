package com.om.timely.faculty.timetable_generation.data.repository

import com.om.timely.BuildConfig
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.faculty.timetable_generation.data.mapper.toDomain
import com.om.timely.faculty.timetable_generation.data.mapper.toDomainGroup
import com.om.timely.faculty.timetable_generation.data.mapper.toDto
import com.om.timely.faculty.timetable_generation.domain.error.TimetableError
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetableJobRequestDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetableJobResponseDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetableJobStatusDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetableJobResultDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetableJobResultDataDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetablePdfExportRequestDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetablePdfExportResponseDto
import com.om.timely.faculty.timetable_generation.domain.model.TimetableGenerationResult
import com.om.timely.faculty.timetable_generation.domain.model.TimetableJobStatusModel
import com.om.timely.faculty.timetable_generation.domain.model.TimetablePdfAsset
import com.om.timely.faculty.timetable_generation.domain.model.TimetableTemplate
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import com.om.timely.faculty.timetable_generation.data.remote.dto.PublishTimetableAssignmentDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.PublishTimetableBulkRequestDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.PublishTimetableDayDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.PublishTimetableSlotDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.PublishTimetableTemplateDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetableGroupDto
import com.om.timely.faculty.timetable_generation.domain.model.TimetableGroup
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.request.parameter

class RemoteTimetableRepository(
    private val httpClient: HttpClient
) : TimetableRepository {

    override suspend fun getGeneratedTemplate(semester: Int, division: String): TimetableTemplate? {
        // Implement when backend support for fetching existing timetables is ready
        return null
    }

    override suspend fun startGenerationJob(
        syllabusId: Int,
        department: String,
        division: String,
        fcmToken: String
    ): Result<String, TimetableError> {
        return safeCall<TimetableJobResponseDto> {
            httpClient.post("${BuildConfig.BASE_URL}/api/timetables/jobs") {
                contentType(ContentType.Application.Json)
                setBody(
                    TimetableJobRequestDto(
                        syllabusId = syllabusId,
                        dept = department,
                        division = division,
                        fcmToken = fcmToken
                    )
                )
            }
        }.map { responseDto ->
            if (responseDto.status != "failed") {
                responseDto.jobId
            } else {
                throw Exception(responseDto.message)
            }
        }.mapError { mapToTimetableError(it) }
    }

    override suspend fun getJobStatus(jobId: String): Result<TimetableJobStatusModel, TimetableError> {
        return safeCall<TimetableJobStatusDto> {
            httpClient.get("${BuildConfig.BASE_URL}/api/timetables/jobs/$jobId")
        }.map { dto ->
            TimetableJobStatusModel(
                jobId = dto.jobId,
                status = dto.status,
                stage = dto.stage,
                error = dto.error,
                warnings = dto.warnings
            )
        }.mapError { mapToTimetableError(it) }
    }

    override suspend fun getJobResult(jobId: String): Result<TimetableGenerationResult, TimetableError> {
        return safeCall<TimetableJobResultDto> {
            httpClient.get("${BuildConfig.BASE_URL}/api/timetables/jobs/$jobId/result")
        }.map { dto ->
            if (dto.status == "completed" && dto.data != null) {
                val templates = dto.data.timetables.map { it.toDomain() }
                TimetableGenerationResult(
                    templates = templates,
                    warnings = dto.warnings
                )
            } else {
                throw Exception("Result not complete or data is missing")
            }
        }.mapError { mapToTimetableError(it) }
    }

    private fun mapToTimetableError(error: NetworkError): TimetableError {
        return when (error) {
            NetworkError.NO_INTERNET -> TimetableError.Network(NetworkError.NO_INTERNET)
            NetworkError.SERVER_ERROR -> TimetableError.Network(NetworkError.SERVER_ERROR)
            NetworkError.SERIALIZATION -> TimetableError.Network(NetworkError.SERIALIZATION)
            else -> TimetableError.BackendError(error.name)
        }
    }

    override suspend fun saveTimetables(groupName: String, templates: List<TimetableTemplate>): Result<Unit, TimetableError> {
        val dtos = templates.map { template ->
            PublishTimetableTemplateDto(
                dept = template.department,
                semester = template.semester,
                division = template.division,
                name = "Sem ${template.semester} ${template.division}",
                days = template.days.map { day ->
                    PublishTimetableDayDto(
                        dayOfWeek = day.dayOfWeek,
                        slots = day.slots.filter { !it.isEmpty }.map { slot ->
                            PublishTimetableSlotDto(
                                slotIndex = slot.slotIndex,
                                startTime = slot.startTime,
                                endTime = slot.endTime,
                                slotKind = when (slot.slotKind.apiValue) {
                                    "tutorial" -> "lecture"
                                    "wholeprac" -> "practical"
                                    else -> slot.slotKind.apiValue
                                },
                                title = slot.title,
                                assignments = slot.assignments.map { a ->
                                    PublishTimetableAssignmentDto(
                                        subjectCode = a.subject.code,
                                        subjectName = a.subject.name,
                                        facultyName = a.faculty.name,
                                        roomLabel = "TBD",
                                        batchLabel = a.batchLabel
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }

        val bulkDto = PublishTimetableBulkRequestDto(
            groupName = groupName,
            templates = dtos
        )

        try {
            val jsonString = kotlinx.serialization.json.Json { prettyPrint = true }.encodeToString(
                PublishTimetableBulkRequestDto.serializer(), bulkDto
            )
            android.util.Log.d("TimetableJSON", "Timetable bulk insert payload:\n$jsonString")

            try {
                val tempFile = java.io.File(System.getProperty("java.io.tmpdir"), "timetable_draft.json")
                tempFile.writeText(jsonString)
                android.util.Log.d("TimetableJSON", "JSON saved to: ${tempFile.absolutePath}")
            } catch (e: Exception) {
                android.util.Log.e("TimetableJSON", "Failed to write JSON to file", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("TimetableJSON", "Failed to serialize timetable payload", e)
        }

        // Make the network call
        return safeCall<Any> {
            httpClient.post("${BuildConfig.BASE_URL}/api/timetables/bulk") {
                contentType(ContentType.Application.Json)
                setBody(bulkDto)
            }
        }.map { }.mapError { mapToTimetableError(it) }
    }


    override suspend fun getTimetableGroupsDetails(dept: String): Result<List<TimetableGroup>, TimetableError> {
        return safeCall<List<TimetableGroupDto>> {
            httpClient.get("${BuildConfig.BASE_URL}/api/timetables/groups") {
                parameter("dept", dept)
            }
        }.map { dtos ->
            dtos.map { it.toDomainGroup() }
        }.mapError { mapToTimetableError(it) }
    }

    override suspend fun getTimetableGroup(groupId: Int): Result<TimetableGroup, TimetableError> {
        return safeCall<TimetableGroupDto> {
            httpClient.get("${BuildConfig.BASE_URL}/api/timetables/groups/$groupId")
        }.map { it.toDomainGroup() }
        .mapError { mapToTimetableError(it) }
    }

    override suspend fun activateGroup(groupId: Int, dept: String?): Result<Unit, TimetableError> {
        return safeCall<Any> {
            httpClient.put("${BuildConfig.BASE_URL}/api/timetables/groups/$groupId/activate") {
                dept?.let { parameter("dept", it) }
            }
        }.map { }.mapError { mapToTimetableError(it) }
    }

    override suspend fun deleteGroup(groupId: Int): Result<Unit, TimetableError> {
        return safeCall<Unit> {
            httpClient.delete("${BuildConfig.BASE_URL}/api/timetables/groups/$groupId")
        }.mapError { mapToTimetableError(it) }
    }

    override suspend fun exportGroupToPdf(groupId: Int): Result<List<TimetablePdfAsset>, TimetableError> {
        return safeCall<TimetablePdfExportResponseDto> {
            httpClient.post("${BuildConfig.BASE_URL}/api/timetables/groups/$groupId/export/pdf")
        }.map { responseDto ->
            responseDto.assets.map { asset ->
                TimetablePdfAsset(
                    label = asset.label,
                    group = asset.group,
                    semester = asset.semester,
                    filename = asset.filename,
                    url = asset.url,
                    secureUrl = asset.secureUrl
                )
            }
        }.mapError { mapToTimetableError(it) }
    }

    override suspend fun exportToPdf(templates: List<TimetableTemplate>): Result<List<TimetablePdfAsset>, TimetableError> {
        val templateDtos = templates.map { it.toDto() }
        val requestDto = TimetablePdfExportRequestDto(
            data = TimetableJobResultDataDto(timetables = templateDtos)
        )
        return safeCall<TimetablePdfExportResponseDto> {
            httpClient.post("${BuildConfig.BASE_URL}/api/timetables/export/pdf") {
                contentType(ContentType.Application.Json)
                setBody(requestDto)
            }
        }.map { responseDto ->
            responseDto.assets.map { asset ->
                TimetablePdfAsset(
                    label = asset.label,
                    group = asset.group,
                    semester = asset.semester,
                    filename = asset.filename,
                    url = asset.url,
                    secureUrl = asset.secureUrl
                )
            }
        }.mapError { mapToTimetableError(it) }
    }
}
