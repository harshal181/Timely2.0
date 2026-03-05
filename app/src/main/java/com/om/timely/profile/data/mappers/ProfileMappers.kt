package com.om.timely.profile.data.mappers

import com.om.timely.auth.domain.UserRole
import com.om.timely.profile.data.dto.FacultyDetailDto
import com.om.timely.profile.data.dto.FacultyOutDto
import com.om.timely.profile.data.dto.FullProfileOutDto
import com.om.timely.profile.data.dto.StudentDetailsDto
import com.om.timely.profile.data.dto.StudentOutDto
import com.om.timely.profile.data.dto.UpdateUserDto
import com.om.timely.profile.domain.model.FacultyProfile
import com.om.timely.profile.domain.model.FullProfile
import com.om.timely.profile.domain.model.StudentProfile

fun FullProfileOutDto.toDomain(): FullProfile {
    return FullProfile(
        id = id,
        name = name,
        email = email,
        role = try { UserRole.valueOf(role.uppercase()) } catch(e: Exception) { UserRole.STUDENT },
        studentDetails = studentDetails?.toDomain(),
        facultyDetails = facultyDetails?.toDomain()
    )
}

fun FullProfile.toDto(): UpdateUserDto {
    return UpdateUserDto(
        name = name,
        studentDetails = studentDetails?.toDto(),
        facultyDetails = facultyDetails?.toDto()
    )
}

fun FacultyProfile.toDto(): FacultyDetailDto {
    return FacultyDetailDto(
        designation = designation,
        office = office
    )
}
fun StudentProfile.toDto(): StudentDetailsDto {
    return StudentDetailsDto(
        semester = semester,
        division = division,
        batch = batch
    )
}

fun StudentOutDto.toDomain(): StudentProfile {
    return StudentProfile(
        studId = studId,
        semester = semester,
        division = division,
        batch = batch,
        course = course
    )
}

fun FacultyOutDto.toDomain(): FacultyProfile {
    return FacultyProfile(
        facId = facId,
        designation = designation,
        office = office
    )
}
