package com.om.timely.faculty.syllabus_extractor.presentation.mapper

import com.om.timely.faculty.syllabus_extractor.domain.model.SemesterData
import com.om.timely.faculty.syllabus_extractor.domain.model.SubjectData
import com.om.timely.faculty.syllabus_extractor.presentation.model.ExtractedSubjectUi
import com.om.timely.faculty.syllabus_extractor.presentation.model.SemesterUi

fun SubjectData.toUiModel(): ExtractedSubjectUi {
    return ExtractedSubjectUi(
        id = this.id,
        name = this.name,
        courseCode = this.courseCode,
        lecture = this.lecture.toString(),
        tutorial = this.tutorial.toString(),
        practical = this.practical.toString(),
        type = this.type.lowercase().replaceFirstChar { it.uppercase() }, // "CORE" -> "Core"
        electiveGroup = this.electiveGroup
    )
}

fun List<SemesterData>.toUiModel(): List<SemesterUi> {
    return this.map { semesterData ->
        SemesterUi(
            semester = semesterData.semester,
            subjects = semesterData.subjects.map { it.toUiModel() }
        )
    }
}