package com.om.timely.core.presentation.util

object Constants {
    // Departments
    val DEPARTMENTS = mapOf(
        "CSE" to "Computer Science and Engineering",
        "IT" to "Information Technology",
        "EXTC" to "Electronics and Telecommunication Engineering",
        "MECH" to "Mechanical Engineering",
        "CIVIL" to "Civil Engineering"
    )

    // Subject Types (matching FastAPI literals)
    val SUBJECT_TYPES = listOf("core", "lab", "elective", "project", "internship")

    // You can also move other magic numbers here!
    const val MAX_SEMESTERS = 8
}