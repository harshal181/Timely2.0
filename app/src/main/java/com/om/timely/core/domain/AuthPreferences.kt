package com.om.timely.core.domain

// token manager interface can be easily updated or scaled
interface AuthPreferences {
    fun getAccessToken(): String?
    fun saveTokens(accessToken: String, refreshToken: String)
    fun getRefreshToken(): String?
    fun saveRefreshToken(token: String)
    fun getUserRole(): String?
    fun saveUserRole(role: String)
    fun clearTokens()
}
