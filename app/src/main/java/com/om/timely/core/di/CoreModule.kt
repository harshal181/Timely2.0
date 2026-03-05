package com.om.timely.core.di

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.om.timely.BuildConfig
import com.om.timely.core.data.AuthPreferencesImpl
import com.om.timely.core.data.dto.RefreshRequest
import com.om.timely.core.data.dto.TokenResponse
import com.om.timely.core.domain.AuthPreferences
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {

    single<SharedPreferences> {
        val masterKey = MasterKey.Builder(androidContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        EncryptedSharedPreferences.create(
            androidContext(),
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<AuthPreferences> { 
        AuthPreferencesImpl(get()) 
    }

    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.v("KtorNetwork", message)
                    }
                }
                level = LogLevel.ALL
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val prefs = get<AuthPreferences>()
                        val accessToken = prefs.getAccessToken()
                        val refreshToken = prefs.getRefreshToken()
                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else null
                    }
                    refreshTokens {
                        val prefs = get<AuthPreferences>()
                        val currentRefreshToken = prefs.getRefreshToken() ?: return@refreshTokens null
                        
                        val refreshClient = HttpClient(Android) {
                            install(ContentNegotiation) {
                                json(Json { ignoreUnknownKeys = true })
                            }
                        }
                        
                        try {
                            val response = refreshClient.post("${BuildConfig.BASE_URL}/api/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshRequest(refreshToken = currentRefreshToken))
                            }
                            
                            if (response.status == HttpStatusCode.OK) {
                                val refreshResponse = response.body<TokenResponse>()
                                prefs.saveTokens(
                                    refreshResponse.accessToken,
                                    refreshResponse.refreshToken
                                )
                                BearerTokens(refreshResponse.accessToken, refreshResponse.refreshToken)
                            } else {
                                prefs.clearTokens()
                                null
                            }
                        } catch (e: Exception) {
                            null
                        } finally {
                            refreshClient.close()
                        }
                    }
                }
            }
        }
    }
}
