package com.om.timely.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey
    val id: Int,
    val syllabusId: Int,
    val name: String,
    val code: String,
    val lecture: Int,
    val tutorial: Int,
    val practical: Int,
    val type: String,
    val abbreviation: String,
    val semester: Int,
    val department: String,
    val electiveGroup: String?,
    val isSelected: Boolean,
    val colorArgb: Int
)
