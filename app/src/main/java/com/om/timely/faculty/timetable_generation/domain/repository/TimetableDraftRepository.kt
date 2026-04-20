package com.om.timely.faculty.timetable_generation.domain.repository

import com.om.timely.faculty.timetable_generation.domain.model.TimetableDraft
import com.om.timely.faculty.timetable_generation.domain.model.TimetableTemplate
import kotlinx.coroutines.flow.Flow

interface TimetableDraftRepository {
    /** Persist the current editor state as a named draft. Returns the new draft ID. */
    suspend fun saveDraft(
        label: String,
        dept: String,
        division: String,
        syllabusId: Int,
        syllabusName: String,
        templates: List<TimetableTemplate>,
        warnings: List<String>
    ): Int

    /** Overwrite an existing draft's template list (e.g. after editing). */
    suspend fun updateDraft(draftId: Int, templates: List<TimetableTemplate>)

    /** Observe all saved drafts. */
    fun getAllDrafts(): Flow<List<TimetableDraft>>

    /** Load a single draft for editing. */
    suspend fun getDraftById(id: Int): TimetableDraft?

    /** Remove a draft permanently. */
    suspend fun deleteDraft(id: Int)
}
