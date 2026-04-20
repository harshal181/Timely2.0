package com.om.timely.faculty.timetable_generation.data.repository

import com.om.timely.core.data.local.dao.TimetableDraftDao
import com.om.timely.core.data.local.entity.TimetableDraftEntity
import com.om.timely.faculty.timetable_generation.data.local.toJson
import com.om.timely.faculty.timetable_generation.data.local.toTemplateList
import com.om.timely.faculty.timetable_generation.data.local.toWarningsJson
import com.om.timely.faculty.timetable_generation.data.local.toWarningsList
import com.om.timely.faculty.timetable_generation.domain.model.TimetableDraft
import com.om.timely.faculty.timetable_generation.domain.model.TimetableTemplate
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableDraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalTimetableDraftRepository(
    private val dao: TimetableDraftDao
) : TimetableDraftRepository {

    override suspend fun saveDraft(
        label: String,
        dept: String,
        division: String,
        syllabusId: Int,
        syllabusName: String,
        templates: List<TimetableTemplate>,
        warnings: List<String>
    ): Int {
        val entity = TimetableDraftEntity(
            label = label,
            dept = dept,
            division = division,
            syllabusId = syllabusId,
            syllabusName = syllabusName,
            templatesJson = templates.toJson(),
            warningsJson = warnings.toWarningsJson()
        )
        return dao.insertDraft(entity).toInt()
    }

    override suspend fun updateDraft(draftId: Int, templates: List<TimetableTemplate>) {
        val existing = dao.getDraftById(draftId) ?: return
        dao.insertDraft(existing.copy(templatesJson = templates.toJson()))
    }

    override fun getAllDrafts(): Flow<List<TimetableDraft>> =
        dao.getAllDraftsFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun getDraftById(id: Int): TimetableDraft? =
        dao.getDraftById(id)?.toDomain()

    override suspend fun deleteDraft(id: Int) = dao.deleteDraftById(id)

    // ── mapper ─────────────────────────────────────────────────────────────────
    private fun TimetableDraftEntity.toDomain() = TimetableDraft(
        id = id,
        label = label,
        dept = dept,
        division = division,
        syllabusId = syllabusId,
        syllabusName = syllabusName,
        templates = templatesJson.toTemplateList(),
        warnings = warningsJson.toWarningsList(),
        savedAt = savedAt
    )
}
