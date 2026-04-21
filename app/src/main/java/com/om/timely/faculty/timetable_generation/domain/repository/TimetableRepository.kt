package com.om.timely.faculty.timetable_generation.domain.repository

import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.timetable_generation.domain.error.TimetableError
import com.om.timely.faculty.timetable_generation.domain.model.TimetableGenerationResult
import com.om.timely.faculty.timetable_generation.domain.model.TimetableGroup
import com.om.timely.faculty.timetable_generation.domain.model.TimetableJobStatusModel
import com.om.timely.faculty.timetable_generation.domain.model.TimetablePdfAsset
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
     * Start the generation job. Returns the pending job ID.
     */
    suspend fun startGenerationJob(
        syllabusId: Int,
        department: String,
        division: String,
        fcmToken: String
    ): Result<String, TimetableError>

    /**
     * Query the current backend status of the generation job.
     */
    suspend fun getJobStatus(jobId: String): Result<TimetableJobStatusModel, TimetableError>

    /**
     * Fetch the final generated payload once the job status shows completed.
     */
    suspend fun getJobResult(jobId: String): Result<TimetableGenerationResult, TimetableError>

    /**
     * Persist the user-reviewed (possibly reordered) timetables back to the backend in bulk.
     * Starts a new "Timetable Group" (instance).
     */
    suspend fun saveTimetables(groupName: String, templates: List<TimetableTemplate>): Result<Unit, TimetableError>

    /**
     * Export the given timetable templates to PDF via the backend and return the uploaded assets.
     */
    suspend fun exportToPdf(templates: List<TimetableTemplate>): Result<List<TimetablePdfAsset>, TimetableError>

    /**
     * List all timetable versions (groups) for the given department, including full nested templates and slots.
     */
    suspend fun getTimetableGroupsDetails(dept: String): Result<List<TimetableGroup>, TimetableError>

    /**
     * Fetch a single group's full details (temples, days, slots).
     */
    suspend fun getTimetableGroup(groupId: Int): Result<TimetableGroup, TimetableError>

    /**
     * Set a specific version as active for the department.
     */
    suspend fun activateGroup(groupId: Int, dept: String? = null): Result<Unit, TimetableError>

    /**
     * Delete a timetable version and all its nested templates.
     */
    suspend fun deleteGroup(groupId: Int): Result<Unit, TimetableError>

    /**
     * Export an entire timetable group (all its templates) to PDF.
     */
    suspend fun exportGroupToPdf(groupId: Int): Result<List<TimetablePdfAsset>, TimetableError>
}
