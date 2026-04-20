package com.om.timely.faculty.timetable_generation.domain.repository

import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.timetable_generation.domain.error.TimetableError
import com.om.timely.faculty.timetable_generation.domain.model.TimetableGenerationResult
import com.om.timely.faculty.timetable_generation.domain.model.TimetableJobStatusModel
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
     */
    suspend fun saveTimetables(templates: List<TimetableTemplate>)
}
