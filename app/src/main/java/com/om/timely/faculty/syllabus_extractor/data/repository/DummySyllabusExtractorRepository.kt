package com.om.timely.faculty.syllabus_extractor.data.repository

import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.syllabus_extractor.data.dto.SyllabusExtractionResponseDto
import com.om.timely.faculty.syllabus_extractor.domain.error.SyllabusProcessingError
import com.om.timely.faculty.syllabus_extractor.domain.model.SemesterData
import com.om.timely.faculty.syllabus_extractor.domain.model.SubjectData
import com.om.timely.faculty.syllabus_extractor.domain.model.SubjectExtractionData
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusDraft
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusJobUpdate
import com.om.timely.faculty.syllabus_extractor.domain.repository.SyllabusExtractorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.util.UUID

class DummySyllabusExtractorRepository : SyllabusExtractorRepository {

    override suspend fun startExtractionJob(imageUrls: List<String>): Result<String, SyllabusProcessingError> {
        delay(1000)
        return Result.Success("dummy_job_id")
    }

    override fun observeExtractionJob(jobId: String): Flow<SyllabusJobUpdate> = flow {
        emit(SyllabusJobUpdate.Progress("pending", "Connecting to dummy backend..."))
        delay(1000)
        emit(SyllabusJobUpdate.Progress("processing", "Vroom vroom... extracting fast!"))
        delay(1000)
        emit(SyllabusJobUpdate.Progress("parsing", "Structuring the fake data..."))
        delay(1000)

        val dummyJson = """
            {"semesters":[{"semester":"I","subjects":[{"name":"Essentials of Chemistry","course_code":"BCS1T01","lecture":2,"tutorial":0,"practical":0,"type":"core","elective_group":null},{"name":"Essentials of Chemistry Lab","course_code":"BCS1P01","lecture":0,"tutorial":0,"practical":2,"type":"lab","elective_group":null},{"name":"Applied Algebra","course_code":"BCS1T02","lecture":3,"tutorial":0,"practical":0,"type":"core","elective_group":null},{"name":"Problem Solving using 'C'","course_code":"BCS1T03","lecture":3,"tutorial":0,"practical":0,"type":"core","elective_group":null},{"name":"Problem Solving using 'C' Lab","course_code":"BCS1P03","lecture":0,"tutorial":0,"practical":2,"type":"lab","elective_group":null},{"name":"Basics of Electronics Engineering","course_code":"BCS1T04","lecture":3,"tutorial":0,"practical":0,"type":"core","elective_group":null},{"name":"Basics of Electronics Engineering Lab","course_code":"BCS1P04","lecture":0,"tutorial":0,"practical":2,"type":"lab","elective_group":null},{"name":"Web Design Technology","course_code":"BVS1P01","lecture":0,"tutorial":0,"practical":4,"type":"core","elective_group":null},{"name":"Communication Skills","course_code":"BAE1T01","lecture":1,"tutorial":0,"practical":0,"type":"core","elective_group":null},{"name":"Communication Skills Lab","course_code":"BAE1P01","lecture":0,"tutorial":0,"practical":2,"type":"lab","elective_group":null},{"name":"Refer CC Basket","course_code":"BCC1P01","lecture":0,"tutorial":0,"practical":2,"type":"core","elective_group":null}]},{"semester":"III","subjects":[{"name":"Applied Mathematics – III","course_code":"BSC","lecture":3,"tutorial":1,"practical":1,"type":"core","elective_group":null},{"name":"Object Oriented Programming with Java","course_code":"PCC","lecture":3,"tutorial":1,"practical":4,"type":"core","elective_group":null},{"name":"Operating System","course_code":"PCC","lecture":3,"tutorial":0,"practical":3,"type":"core","elective_group":null},{"name":"Computer Architecture & Digital System","course_code":"PCC","lecture":3,"tutorial":1,"practical":4,"type":"core","elective_group":null},{"name":"Ethics in IT","course_code":"PCC","lecture":3,"tutorial":0,"practical":3,"type":"core","elective_group":null},{"name":"Object Oriented Programming with Java Lab","course_code":"PCC","lecture":2,"tutorial":2,"practical":0,"type":"lab","elective_group":null},{"name":"Operating System Lab","course_code":"PCC","lecture":2,"tutorial":2,"practical":0,"type":"lab","elective_group":null},{"name":"Computer Workshop-I Lab","course_code":"PCC","lecture":2,"tutorial":2,"practical":0,"type":"lab","elective_group":null},{"name":"Universal Human Values","course_code":"HSMC","lecture":2,"tutorial":2,"practical":0,"type":"core","elective_group":null},{"name":"Environment Sciences","course_code":"HSMC","lecture":2,"tutorial":0,"practical":0,"type":"core","elective_group":null}]},{"semester":"V","subjects":[{"name":"Artificial Intelligence","course_code":"PCC-CS","lecture":3,"tutorial":1,"practical":0,"type":"core","elective_group":null},{"name":"Artificial Intelligence - Lab","course_code":"PCC-CS","lecture":0,"tutorial":0,"practical":2,"type":"lab","elective_group":null},{"name":"Design & Analysis of Algorithms","course_code":"PCC-CS","lecture":3,"tutorial":1,"practical":0,"type":"core","elective_group":null},{"name":"Design & Analysis of Algorithms - Lab","course_code":"PCC-CS","lecture":0,"tutorial":0,"practical":2,"type":"lab","elective_group":null},{"name":"Software Engineering & Project Management","course_code":"PCC-CS","lecture":3,"tutorial":0,"practical":0,"type":"project","elective_group":null},{"name":"Effective Technical Communication","course_code":"HSMC","lecture":2,"tutorial":0,"practical":0,"type":"core","elective_group":null},{"name":"Professional Skills Lab I","course_code":"ESC","lecture":0,"tutorial":0,"practical":2,"type":"lab","elective_group":null},{"name":"Yoga and Meditation (Audit Course)","course_code":"MC","lecture":2,"tutorial":0,"practical":0,"type":"core","elective_group":null},{"name":"TCP/IP","course_code":"PEC-CS","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Elective-I"},{"name":"Design Patterns","course_code":"PEC-CS","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Elective-I"},{"name":"Data Warehousing and Mining","course_code":"PEC-CS","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Elective-I"}]},{"semester":"VII","subjects":[{"name":"Cryptography & Network Security","course_code":"BTECHCSE701T","lecture":3,"tutorial":1,"practical":0,"type":"core","elective_group":null},{"name":"Cryptography & Network Security","course_code":"BTECHCSE701P","lecture":0,"tutorial":2,"practical":1,"type":"core","elective_group":null},{"name":"Project","course_code":"BTECHCSE705T","lecture":0,"tutorial":6,"practical":3,"type":"project","elective_group":null},{"name":"Research Methodology (Audit Course)","course_code":"BTECHCSE706T","lecture":2,"tutorial":0,"practical":0,"type":"core","elective_group":null},{"name":"Deep Learning","course_code":"BTECHCSE702T","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Elective-IV"},{"name":"Optimization Techniques","course_code":"BTECHCSE702T","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Elective-IV"},{"name":"Gaming Architecture","course_code":"BTECHCSE702T","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Elective-IV"},{"name":"Natural Language Processing","course_code":"BTECHCSE703T","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Elective-V"},{"name":"Big Data Analytics","course_code":"BTECHCSE703T","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Elective-V"},{"name":"Mobile Computing","course_code":"BTECHCSE703T","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Elective-V"},{"name":"Python Programming","course_code":"BTECHCSE704T","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Open Elective-II"},{"name":"JAVA Programming","course_code":"BTECHCSE704T","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Open Elective-II"},{"name":"Basics of Database Management System","course_code":"BTECHCSE704T","lecture":3,"tutorial":0,"practical":0,"type":"elective","elective_group":"Open Elective-II"}]}]}
        """.trimIndent()

        val jsonConfig = Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }
        val rootData = jsonConfig.decodeFromString<SyllabusExtractionResponseDto>(dummyJson)

        val domainSemesters = rootData.semesters.map { semDto ->
            SemesterData(
                semester = semDto.semester ?: "Unknown",
                subjects = semDto.subjects.map { subjDto ->
                    SubjectData(
                        id = UUID.randomUUID().toString(),
                        name = subjDto.name,
                        courseCode = subjDto.courseCode,
                        lecture = subjDto.lecture,
                        tutorial = subjDto.tutorial,
                        practical = subjDto.practical,
                        type = subjDto.type ?: "Core",
                        electiveGroup = subjDto.electiveGroup
                    )
                }
            )
        }
        
        emit(SyllabusJobUpdate.Completed(SubjectExtractionData(semesters = domainSemesters)))
    }



    override suspend fun confirmAndSaveSubjects(
        name: String,
        dept: String,
        termType: String,
        semesters: List<SemesterData>
    ): Result<Unit, SyllabusProcessingError> {
        TODO("Not yet implemented")
    }

    override suspend fun saveDraft(
        id: Int?,
        name: String,
        dept: String,
        termType: String,
        semesters: List<SemesterData>
    ): Result<Unit, SyllabusProcessingError> {
        TODO("Not yet implemented")
    }

    override fun getAllDrafts(): Flow<List<SyllabusDraft>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDraft(id: Int): Result<Unit, SyllabusProcessingError> {
        TODO("Not yet implemented")
    }

    override suspend fun getDraftById(id: Int): SyllabusDraft? {
        TODO("Not yet implemented")
    }


}
