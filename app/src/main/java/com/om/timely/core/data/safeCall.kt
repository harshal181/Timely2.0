package com.om.timely.core.data

import android.util.Log
import com.om.timely.core.domain.util.NetworkError
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import com.om.timely.core.domain.util.Result
import io.ktor.client.statement.HttpResponse

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, NetworkError>{
    val response = try{
        execute()
    }
    catch (e: UnresolvedAddressException){
        return Result.Error(NetworkError.NO_INTERNET)
    }
    catch (e: NoTransformationFoundException){
        return Result.Error(NetworkError.SERIALIZATION_ERROR)
    }
    catch (e: Exception){
        Log.e("AuthDebug", "CRITICAL FAILURE: ${e.message}", e)
        coroutineContext.ensureActive()
        return Result.Error(NetworkError.UNKNOWN)
    }
    return responseToResult(response)
}