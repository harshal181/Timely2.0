package com.om.timely.faculty.timetable_generation.domain.repository

import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.timetable_generation.domain.error.TimetableError
import com.om.timely.faculty.timetable_generation.domain.model.TimetableGenerationResult
import com.om.timely.faculty.timetable_generation.domain.model.TimetableTemplate

/**
 * Contract that any timetable data source must satisfy.
 *
 * The presentation layer depends only on this interface; the actual
 * implementation (DummyTimetableRepository, RemoteTimetableRepository, …)
 * lives in the :data layer and is injected via DI.
 */
interface TimetableRepository {

    /**
     * Fetch the generated timetable template for a specific semester and division.
     * Returns null if no template has been generated yet.
     */
    suspend fun getGeneratedTemplate(semester: Int, division: String): TimetableTemplate?

    /**
     * Trigger backend generation for the given schema.
     * Returns the freshly-generated [TimetableTemplate] when the solver finishes.
     */
    suspend fun generateTimetable(
        syllabusId: Int,
        department: String,
        division: String
    ): Result<TimetableGenerationResult, TimetableError>

    /**
     * Persist the user-reviewed (possibly reordered) timetable back to the backend.
     */
    suspend fun saveTimetable(template: TimetableTemplate)
}
