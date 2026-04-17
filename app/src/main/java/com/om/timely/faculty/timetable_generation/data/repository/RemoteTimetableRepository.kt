package com.om.timely.faculty.timetable_generation.data.repository

import com.om.timely.BuildConfig
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.faculty.timetable_generation.data.mapper.toDomain
import com.om.timely.faculty.timetable_generation.domain.error.TimetableError
import com.om.timely.faculty.timetable_generation.data.remote.dto.GenerateTimetableRequestDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.GenerateTimetableResponseDto
import com.om.timely.faculty.timetable_generation.domain.model.TimetableGenerationResult
import com.om.timely.faculty.timetable_generation.domain.model.TimetableTemplate
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteTimetableRepository(
    private val httpClient: HttpClient
) : TimetableRepository {

    override suspend fun getGeneratedTemplate(semester: Int, division: String): TimetableTemplate? {
        // Implement when backend support for fetching existing timetables is ready
        return null
    }

    override suspend fun generateTimetable(
        syllabusId: Int,
        department: String,
        division: String
    ): Result<TimetableGenerationResult, TimetableError> {
        return safeCall<GenerateTimetableResponseDto> {
            httpClient.post("${BuildConfig.BASE_URL}/api/timetables/generate") {
                contentType(ContentType.Application.Json)
                setBody(
                    GenerateTimetableRequestDto(
                        syllabusId = syllabusId,
                        dept = department,
                        division = division
                    )
                )
            }
        }.map { responseDto ->
            if (responseDto.status == "SUCCESS") {
                val templates = responseDto.data?.map { it.toDomain() } ?: emptyList()
                if (templates.isNotEmpty()) {
                    TimetableGenerationResult(
                        templates = templates,
                        warnings = responseDto.metadata.warnings
                    )
                } else {
                    throw Exception("Response did not contain timetable data.")
                }
            } else {
                throw Exception(responseDto.message)
            }
        }.mapError { error ->
            when (error) {
                NetworkError.NO_INTERNET -> TimetableError.Network(NetworkError.NO_INTERNET)
                NetworkError.SERVER_ERROR -> TimetableError.Network(NetworkError.SERVER_ERROR)
                NetworkError.SERIALIZATION -> TimetableError.Network(NetworkError.SERIALIZATION)
                else -> TimetableError.BackendError(error.name) // or mapping specific logic
            }
        }
    }

    override suspend fun saveTimetable(template: TimetableTemplate) {
        // Implement when the backend `/api/timetables/{id}/save` is ready
    }
}
