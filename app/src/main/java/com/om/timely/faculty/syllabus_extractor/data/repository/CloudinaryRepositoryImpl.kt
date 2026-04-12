package com.om.timely.faculty.syllabus_extractor.data.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.om.timely.core.domain.util.Error
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.syllabus_extractor.domain.repository.CloudinaryRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CloudinaryRepositoryImpl : CloudinaryRepository {
    override suspend fun uploadImage(uri: Uri): Result<String, Error> {
        return suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        if (secureUrl != null) {
                            continuation.resume(Result.Success(secureUrl))
                        } else {
                            continuation.resume(Result.Error(NetworkError.UNKNOWN))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resume(Result.Error(NetworkError.UNKNOWN))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        continuation.resume(Result.Error(NetworkError.UNKNOWN))
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
        }
    }
}
