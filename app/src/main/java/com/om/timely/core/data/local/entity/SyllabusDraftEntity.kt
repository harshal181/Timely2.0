package com.om.timely.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "syllabus_drafts")
data class SyllabusDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dept: String,
    val termType: String,
    val dataJson: String, // List<SemesterData>
    val createdAt: Long = System.currentTimeMillis()
)
