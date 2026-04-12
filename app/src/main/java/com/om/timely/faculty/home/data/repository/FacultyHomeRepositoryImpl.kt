package com.om.timely.faculty.home.data.repository

import android.util.Log
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.home.domain.model.DailyScheduleStats
import com.om.timely.faculty.home.domain.model.DayScheduleInfo
import com.om.timely.faculty.home.domain.model.QuickStats
import com.om.timely.faculty.home.domain.model.schedule.BatchInfo
import com.om.timely.faculty.home.domain.model.schedule.EmptySlot
import com.om.timely.faculty.home.domain.model.schedule.LectureSlot
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot
import com.om.timely.faculty.home.domain.model.schedule.RecessSlot
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import com.om.timely.core.data.local.dao.TimetableDao
import com.om.timely.core.data.local.entity.TimetableEntity
import com.om.timely.faculty.home.data.dto.ScheduleSlotDto
import com.om.timely.faculty.home.data.dto.timetableJsonParser
import com.om.timely.faculty.home.data.dto.toDomain
import com.om.timely.faculty.home.data.dto.toDto
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

import com.om.timely.faculty.home.domain.util.NotificationScheduler
import com.om.timely.BuildConfig
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.faculty.home.data.dto.TimetableResponseDto
import com.om.timely.faculty.home.domain.error.TimeTableError
import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

import com.om.timely.faculty.home.domain.use_case.ScheduleNotificationsUseCase

class FacultyHomeRepositoryImpl(
    private val timetableDao: TimetableDao,
    private val scheduleNotificationsUseCase: ScheduleNotificationsUseCase,
    private val httpClient: HttpClient
) : FacultyHomeRepository {

    override suspend fun getScheduleForDay(day: String, semester: Int): Result<DayScheduleInfo, TimeTableError> {
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
                // Re-schedule alarms from cache applying the custom rules
                scheduleNotificationsUseCase(timeline, day, semester)

                return Result.Success(mapToDayScheduleInfo(day, semester, timeline))
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

        return safeCall<TimetableResponseDto> {
            httpClient.get("${BuildConfig.BASE_URL}/api/timetables/") {
                parameter("semester", semester)
                parameter("div", "A")
                parameter("day", fullDayName)
            }
        }.map { dto ->
            val jsonString = timetableJsonParser.encodeToString(dto.timeline)
            val entity = TimetableEntity(
                id = "sem_${semester}_${day}",
                semester = semester,
                division = "A",
                dayOfWeek = day,
                timelineJson = jsonString,
                updatedAt = System.currentTimeMillis()
            )
            timetableDao.insertTimetable(entity)

            val domainTimeline = dto.timeline.map { it.toDomain() }
            scheduleNotificationsUseCase(domainTimeline, day, semester)

            mapToDayScheduleInfo(day, semester, domainTimeline)
        }.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> TimeTableError.NO_SCHEDULE_FOUND
                else -> TimeTableError.Network(error)
            }
        }
    }

    /**
     * Helper to keep logic DRY for Stats calculation
     */
    private fun mapToDayScheduleInfo(
        day: String,
        semester: Int,
        timeline: List<ScheduleSlot>
    ): DayScheduleInfo {
        return DayScheduleInfo(
            activeDay = day,
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
