package com.om.timely.faculty.home.data.dto

import com.om.timely.faculty.home.domain.model.schedule.BatchInfo
import com.om.timely.faculty.home.domain.model.schedule.EmptySlot
import com.om.timely.faculty.home.domain.model.schedule.LectureSlot
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot
import com.om.timely.faculty.home.domain.model.schedule.RecessSlot
import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed class ScheduleSlotDto {
    abstract val id: String
    @SerialName("start_time") abstract val startTime: String
    @SerialName("end_time") abstract val endTime: String
}

@Serializable
data class SubjectOutDto(
    val code: String,
    val name: String,
    val abbr: String,
    val sem: Int,
    val dept: String,
    @SerialName("color_argb") val colorArgb: Long
)

@Serializable
@SerialName("lecture")
data class LectureSlotDto(
    override val id: String,
    val subject: SubjectOutDto,
    @SerialName("faculty_name") val facultyName: String,
    @SerialName("room_no") val roomNo: String,
    @SerialName("start_time") override val startTime: String,
    @SerialName("end_time") override val endTime: String
) : ScheduleSlotDto()

@Serializable
@SerialName("practical")
data class PracticalSlotDto(
    override val id: String,
    val title: String = "PRACTICAL SESSION",
    @SerialName("start_time") override val startTime: String,
    @SerialName("end_time") override val endTime: String,
    val batches: List<BatchInfoDto>
) : ScheduleSlotDto()

@Serializable
data class BatchInfoDto(
    @SerialName("batch_name") val batchName: String,
    val subject: SubjectOutDto,
    @SerialName("faculty_name") val facultyName: String,
    @SerialName("room_no") val roomNo: String
)

@Serializable
@SerialName("recess")
data class RecessSlotDto(
    override val id: String,
    val title: String = "Lunch Break",
    @SerialName("start_time") override val startTime: String,
    @SerialName("end_time") override val endTime: String
) : ScheduleSlotDto()

@Serializable
@SerialName("empty")
data class EmptySlotDto(
    override val id: String,
    val title: String = "No class scheduled",
    @SerialName("start_time") override val startTime: String,
    @SerialName("end_time") override val endTime: String
) : ScheduleSlotDto()

// Mappers
fun ScheduleSlot.toDto(): ScheduleSlotDto = when (this) {
    is LectureSlot -> LectureSlotDto(
        id = id, 
        subject = SubjectOutDto(subjectCode, subjectName, subjectCode, 1, "CS", colorArgb), 
        facultyName = facultyName, 
        roomNo = roomNo, 
        startTime = startTime, 
        endTime = endTime
    )
    is PracticalSlot -> PracticalSlotDto(
        id = id, 
        title = title, 
        startTime = startTime, 
        endTime = endTime, 
        batches = batches.map { 
            BatchInfoDto(it.batchName, SubjectOutDto(it.subjectName, it.subjectName, it.subjectName, 1, "CS", colorArgb), it.facultyName, it.roomNo) 
        }
    )
    is RecessSlot -> RecessSlotDto(id, title, startTime, endTime)
    is EmptySlot -> EmptySlotDto(id, title, startTime, endTime)
}

fun ScheduleSlotDto.toDomain(): ScheduleSlot = when (this) {
    is LectureSlotDto -> LectureSlot(
        id = id, 
        subjectCode = subject.code, 
        subjectName = subject.name, 
        facultyName = facultyName, 
        roomNo = roomNo, 
        startTime = startTime, 
        endTime = endTime, 
        colorArgb = subject.colorArgb ?: 0xFF2563EB
    )
    is PracticalSlotDto -> PracticalSlot(
        id = id, 
        title = title, 
        startTime = startTime, 
        endTime = endTime, 
        colorArgb = batches.firstOrNull()?.subject?.colorArgb ?: 0xFF059669, 
        batches = batches.map { 
            BatchInfo(it.batchName, it.batchName, it.subject.name, it.facultyName, it.roomNo) 
        }
    )
    is RecessSlotDto -> RecessSlot(id, title, startTime, endTime)
    is EmptySlotDto -> EmptySlot(id, title, startTime, endTime)
}

fun TimetableSlotResponseDto.toDomain(): ScheduleSlot {
    return when (slotKind) {
        "lecture", "tutorial" -> LectureSlot(
            id = id.toString(),
            subjectCode = assignments.firstOrNull()?.subject?.code ?: "Unknown",
            subjectName = assignments.firstOrNull()?.subject?.name ?: "Unknown",
            facultyName = assignments.firstOrNull()?.faculty?.name ?: "TBD",
            roomNo = assignments.firstOrNull()?.roomLabel ?: "TBD",
            startTime = startTime,
            endTime = endTime,
            colorArgb = assignments.firstOrNull()?.subject?.colorArgb ?: 0xFF2563EB
        )
        "practical", "wholeprac" -> PracticalSlot(
            id = id.toString(),
            title = title ?: "PRACTICAL SESSION",
            startTime = startTime,
            endTime = endTime,
            colorArgb = assignments.firstOrNull()?.subject?.colorArgb ?: 0xFF059669,
            batches = assignments.map {
                BatchInfo(
                    id = it.id.toString(),
                    batchName = it.batchLabel ?: "All",
                    subjectName = it.subject?.name ?: "Unknown",
                    facultyName = it.faculty?.name ?: "TBD",
                    roomNo = it.roomLabel ?: "TBD"
                )
            }
        )
        "recess", "break" -> RecessSlot(
            id = id.toString(),
            title = title ?: "Lunch Break",
            startTime = startTime,
            endTime = endTime
        )
        else -> EmptySlot(
            id = id.toString(),
            title = title ?: "No class scheduled",
            startTime = startTime,
            endTime = endTime
        )
    }
}

fun ScheduleSlot.toScheduleSlotDto(): ScheduleSlotDto = when (this) {
    is LectureSlot -> LectureSlotDto(
        id = id, 
        subject = SubjectOutDto(subjectCode, subjectName, subjectCode, 1, "CS", colorArgb), 
        facultyName = facultyName, 
        roomNo = roomNo, 
        startTime = startTime, 
        endTime = endTime
    )
    is PracticalSlot -> PracticalSlotDto(
        id = id, 
        title = title, 
        startTime = startTime, 
        endTime = endTime, 
        batches = batches.map { 
            BatchInfoDto(it.batchName, SubjectOutDto(it.subjectName, it.subjectName, it.subjectName, 1, "CS", colorArgb), it.facultyName, it.roomNo) 
        }
    )
    is RecessSlot -> RecessSlotDto(id, title, startTime, endTime)
    is EmptySlot -> EmptySlotDto(id, title, startTime, endTime)
}

// Global Parser Config
val timetableJsonParser = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
    serializersModule = SerializersModule {
        polymorphic(ScheduleSlotDto::class) {
            subclass(LectureSlotDto::class)
            subclass(PracticalSlotDto::class)
            subclass(RecessSlotDto::class)
            subclass(EmptySlotDto::class)
        }
    }
}
