package com.om.timely.faculty.subjects.data.mappers

import com.om.timely.faculty.subjects.data.dto.SubjectCreateDto
import com.om.timely.faculty.subjects.data.dto.SubjectDto
import com.om.timely.faculty.subjects.data.dto.SubjectUpdateDto
import com.om.timely.faculty.subjects.domain.model.Subject
import com.om.timely.core.data.local.entity.SubjectEntity
import com.om.timely.core.data.local.entity.SyllabusEntity
import com.om.timely.faculty.subjects.data.dto.SyllabusDto
import com.om.timely.faculty.subjects.domain.model.Syllabus

fun SubjectDto.toDomain(): Subject {
    return Subject(
        id = id,
        syllabusId = syllabusId,
        name = name,
        code = code,
        lecture = lecture,
        tutorial = tutorial,
        practical = practical,
        type = type, // Replaced subjectType
        abbreviation = abbr,
        semester = sem,
        department = dept,
        electiveGroup = electiveGroup,
        isSelected = isSelected,
        colorArgb = colorArgb.toInt() // Convert Long back to Int for Compose UI
    )
}

fun Subject.toCreateDto(): SubjectCreateDto {
    return SubjectCreateDto(
        syllabusId = syllabusId,
        name = name,
        abbr = abbreviation,
        code = code,
        sem = semester,
        lecture = lecture,
        tutorial = tutorial,
        practical = practical,
        type = type,
        electiveGroup = electiveGroup,
        isSelected = isSelected,
        colorArgb = colorArgb.toLong() // Convert Int to Long for safe JSON serialization
    )
}

fun Subject.toUpdateDto(): SubjectUpdateDto {
    // Note: Since SubjectUpdateDto fields are optional (nullable), mapping the entire
    // domain model will send a full update. If you need partial updates (PATCH),
    // you might want to handle this differently in your repository layer.
    return SubjectUpdateDto(
        syllabusId = syllabusId,
        name = name,
        abbr = abbreviation,
        code = code,
        sem = semester,
        dept = department,
        lecture = lecture,
        tutorial = tutorial,
        practical = practical,
        type = type,
        electiveGroup = electiveGroup,
        isSelected = isSelected,
        colorArgb = colorArgb.toLong()
    )
}



fun SubjectEntity.toDomain(): Subject {
    return Subject(
        id = id,
        syllabusId = syllabusId,
        name = name,
        code = code,
        lecture = lecture,
        tutorial = tutorial,
        practical = practical,
        type = type,
        abbreviation = abbreviation,
        semester = semester,
        department = department,
        electiveGroup = electiveGroup,
        isSelected = isSelected,
        colorArgb = colorArgb
    )
}

fun Subject.toEntity(): SubjectEntity {
    return SubjectEntity(
        id = id,
        syllabusId = syllabusId,
        name = name,
        code = code,
        lecture = lecture,
        tutorial = tutorial,
        practical = practical,
        type = type,
        abbreviation = abbreviation,
        semester = semester,
        department = department,
        electiveGroup = electiveGroup,
        isSelected = isSelected,
        colorArgb = colorArgb
    )
}

fun SyllabusDto.toDomain(): Syllabus {
    return Syllabus(
        id = id,
        name = name,
        dept = dept,
        termType = termType,
        createdAt = createdAt
    )
}

fun Syllabus.toEntity(): SyllabusEntity {
    return SyllabusEntity(
        id = id,
        name = name,
        dept = dept,
        termType = termType,
        createdAt = createdAt
    )
}

fun SyllabusEntity.toDomain(): Syllabus {
    return Syllabus(
        id = id,
        name = name,
        dept = dept,
        termType = termType,
        createdAt = createdAt
    )
}
