package com.om.timely.faculty.syllabus_extractor.data.repository

import android.net.Uri
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.faculty.syllabus_extractor.domain.repository.CloudinaryRepository
import kotlinx.coroutines.delay

class DummyCloudinaryRepository : CloudinaryRepository {
    override suspend fun uploadImage(uri: Uri): Result<String, NetworkError> {
        delay(800)
        return Result.Success("https://dummy.cloudinary.com/fake_image.jpg")
    }
}
