package com.om.timely.core.data

import com.om.timely.core.domain.util.NetworkError
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import com.om.timely.core.domain.util.Result

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, NetworkError>{
    return when(response.status.value){
        in 200 .. 299 ->{
            try{
                Result.Success(response.body<T>())
            }catch (e: NoTransformationFoundException){
                Result.Error(NetworkError.SERIALIZATION_ERROR)
            }
        }
        401 -> Result.Error(NetworkError.UNAUTHORIZED)
        400 -> Result.Error(NetworkError.BAD_REQ)
        404 -> Result.Error(NetworkError.NOT_FOUND)
        409 -> Result.Error(NetworkError.CONFLICT)
        408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
        429 -> Result.Error(NetworkError.TOO_MANY_REQUEST)
        in 500 .. 599 -> Result.Error(NetworkError.SERVER_ERROR)
        else -> Result.Error(NetworkError.UNKNOWN)

    }
}