package com.om.timely.faculty.syllabus_extractor.domain.repository

import android.net.Uri
import com.om.timely.core.domain.util.Error
import com.om.timely.core.domain.util.Result

interface CloudinaryRepository {
    suspend fun uploadImage(uri: Uri): Result<String, Error>
}
