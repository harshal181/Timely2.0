package com.om.timely.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.om.timely.core.data.local.dao.NotificationDao
import com.om.timely.core.data.local.dao.SubjectDao
import com.om.timely.core.data.local.dao.TimetableDao
import com.om.timely.core.data.local.dao.SyllabusDao
import com.om.timely.core.data.local.entity.SubjectEntity
import com.om.timely.core.data.local.entity.TimetableEntity
import com.om.timely.core.data.local.entity.SyllabusEntity
import com.om.timely.core.data.local.dao.SyllabusDraftDao
import com.om.timely.core.data.local.dao.TimetableDraftDao
import com.om.timely.core.data.local.entity.NotificationEntity
import com.om.timely.core.data.local.entity.SyllabusDraftEntity
import com.om.timely.core.data.local.entity.TimetableDraftEntity

@Database(
    entities = [TimetableEntity::class, SubjectEntity::class, SyllabusEntity::class, NotificationEntity::class, SyllabusDraftEntity::class, TimetableDraftEntity::class],
    version = 6,
    exportSchema = false
)
abstract class TimelyDatabase : RoomDatabase() {
    abstract val timetableDao: TimetableDao
    abstract val subjectDao: SubjectDao
    abstract val syllabusDao: SyllabusDao
    abstract val notificationDao: NotificationDao
    abstract val syllabusDraftDao: SyllabusDraftDao
    abstract val timetableDraftDao: TimetableDraftDao
    
    companion object {
        const val DATABASE_NAME = "timely_db"
    }
}
