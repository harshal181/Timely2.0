package com.om.timely.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "syllabuses")
data class SyllabusEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val dept: String,
    val termType: String,
    val createdAt: String
)
