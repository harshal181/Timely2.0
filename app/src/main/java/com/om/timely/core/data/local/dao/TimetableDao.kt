package com.om.timely.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.om.timely.core.data.local.entity.TimetableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(timetable: TimetableEntity)

    @Query("SELECT * FROM timetables WHERE semester = :semester AND dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getTimetableForDay(semester: Int, dayOfWeek: String): TimetableEntity?

    @Query("SELECT * FROM timetables WHERE semester = :semester")
    suspend fun getTimetablesForSemester(semester: Int): List<TimetableEntity>

    @Query("DELETE FROM timetables")
    suspend fun clearTimetables()
}
