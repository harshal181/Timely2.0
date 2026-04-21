package com.om.timely.faculty.home.data.repository

import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.home.domain.model.DailyScheduleStats
import com.om.timely.faculty.home.domain.model.DayScheduleInfo
import com.om.timely.faculty.home.domain.model.QuickStats
import com.om.timely.faculty.home.domain.model.schedule.LectureSlot
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import com.om.timely.core.data.local.dao.TimetableDao
import com.om.timely.core.data.local.entity.TimetableEntity
import com.om.timely.faculty.home.data.dto.ScheduleSlotDto
import com.om.timely.faculty.home.data.dto.timetableJsonParser
import com.om.timely.faculty.home.data.dto.toDomain
import kotlinx.serialization.encodeToString

import com.om.timely.BuildConfig
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.faculty.home.data.dto.TimetableTemplateResponseDto
import com.om.timely.faculty.home.data.dto.CreateTimetableSlotDto
import com.om.timely.faculty.home.data.dto.UpdateTimetableSlotDto
import com.om.timely.faculty.home.data.dto.SwapSlotsDto
import com.om.timely.faculty.home.data.dto.toScheduleSlotDto
import com.om.timely.faculty.home.domain.error.TimeTableError
import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.patch
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType

import com.om.timely.faculty.home.domain.use_case.ScheduleNotificationsUseCase

class FacultyHomeRepositoryImpl(
    private val timetableDao: TimetableDao,
    private val scheduleNotificationsUseCase: ScheduleNotificationsUseCase,
    private val httpClient: HttpClient
) : FacultyHomeRepository {

    override suspend fun getScheduleForDay(day: String, semester: Int, dept: String?): Result<DayScheduleInfo, TimeTableError> {
        // 1. Try to fetch from Local Cache (Room) first
        val cachedEntity = timetableDao.getTimetableForDay(semester, day)

        if (cachedEntity != null) {
            val cachedDomain = try {
                val dtoList = timetableJsonParser.decodeFromString<List<ScheduleSlotDto>>(cachedEntity.timelineJson)
                dtoList.map { it.toDomain() }
            } catch (e: Exception) {
                null
            }

            cachedDomain?.let { timeline ->
                scheduleNotificationsUseCase(timeline, day, semester)

                return Result.Success(mapToDayScheduleInfo(day, cachedEntity.dayId, semester, timeline))
            }
        }

        // 2. Local cache missed or failed to parse -> Fetch from Network using safeCall
        val fullDayName = when (day) {
            "Mon" -> "Monday"
            "Tue" -> "Tuesday"
            "Wed" -> "Wednesday"
            "Thu" -> "Thursday"
            "Fri" -> "Friday"
            "Sat" -> "Saturday"
            else -> "Monday"
        }

        val networkResult = safeCall<List<TimetableTemplateResponseDto>> {
            httpClient.get("${BuildConfig.BASE_URL}/api/timetables/") {
                parameter("dept", dept ?: "Computer Science and Engineering")
                parameter("semester", semester)
            }
        }
        
        return when (networkResult) {
            is Result.Success -> {
                val templates = networkResult.data
                val template = templates.firstOrNull()
                if (template == null) return Result.Error(TimeTableError.NO_SCHEDULE_FOUND)
                
                val dayDto = template.days.find { it.dayOfWeek.equals(day, ignoreCase = true) }
                if (dayDto == null) return Result.Error(TimeTableError.NO_SCHEDULE_FOUND)
                
                val domainTimeline = dayDto.slots.map { it.toDomain() }
                
                val dtoList = domainTimeline.map { it.toScheduleSlotDto() }
                val jsonString = timetableJsonParser.encodeToString(dtoList)
                
                val entity = TimetableEntity(
                    id = "sem_${semester}_${day}",
                    semester = semester,
                    division = "A",
                    dayOfWeek = day,
                    dayId = dayDto.id,
                    timelineJson = jsonString,
                    updatedAt = System.currentTimeMillis()
                )
                timetableDao.insertTimetable(entity)

                scheduleNotificationsUseCase(domainTimeline, day, semester)

                Result.Success(mapToDayScheduleInfo(day, dayDto.id, semester, domainTimeline))
            }
            is Result.Error -> {
                Result.Error(when (networkResult.error) {
                    NetworkError.NOT_FOUND -> TimeTableError.NO_SCHEDULE_FOUND
                    else -> TimeTableError.Network(networkResult.error)
                })
            }
        }
    }

    override suspend fun addSlot(dayId: Int, slotData: CreateTimetableSlotDto): Result<Unit, TimeTableError> {
        return safeCall<Any> {
            httpClient.post("${BuildConfig.BASE_URL}/api/timetables/days/$dayId/slots") {
                contentType(ContentType.Application.Json)
                setBody(slotData)
            }
        }.map { }.mapError { TimeTableError.Network(it) }
    }

    override suspend fun updateSlot(slotId: Int, slotData: UpdateTimetableSlotDto): Result<Unit, TimeTableError> {
        return safeCall<Any> {
            httpClient.patch("${BuildConfig.BASE_URL}/api/timetables/slots/$slotId") {
                contentType(ContentType.Application.Json)
                setBody(slotData)
            }
        }.map { }.mapError { TimeTableError.Network(it) }
    }

    override suspend fun swapSlots(dayId: Int, firstSlotId: Int, secondSlotId: Int): Result<Unit, TimeTableError> {
        return safeCall<Any> {
            httpClient.post("${BuildConfig.BASE_URL}/api/timetables/days/$dayId/swap") {
                contentType(ContentType.Application.Json)
                setBody(SwapSlotsDto(firstSlotId, secondSlotId))
            }
        }.map { }.mapError { TimeTableError.Network(it) }
    }

    override suspend fun deleteSlot(slotId: Int): Result<Unit, TimeTableError> {
        return safeCall<Any> {
            httpClient.delete("${BuildConfig.BASE_URL}/api/timetables/slots/$slotId")
        }.map { }.mapError { TimeTableError.Network(it) }
    }

    /**
     * Helper to keep logic DRY for Stats calculation
     */
    private fun mapToDayScheduleInfo(
        day: String,
        dayId: Int?,
        semester: Int,
        timeline: List<ScheduleSlot>
    ): DayScheduleInfo {
        return DayScheduleInfo(
            activeDay = day,
            dayId = dayId,
            dailyStats = DailyScheduleStats(
                totalLectures = timeline.filterIsInstance<LectureSlot>().size,
                totalLabs = timeline.filterIsInstance<PracticalSlot>().size,
                uniqueStudents = if (semester == 2) 150 else 280
            ),
            quickStats = QuickStats(
                subjects = if (semester == 2) 4 else 5,
                batches = if (semester == 2) 2 else 3,
                hoursPerWeek = if (semester == 2) 14 else 18
            ),
            timeline = timeline
        )
    }
}
