package com.om.timely.core.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.om.timely.core.domain.AuthPreferences

class AuthPreferencesImpl(
    private val sharedPreferences: SharedPreferences
): AuthPreferences {

    override fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit(commit = true) {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }


    override fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override fun saveRefreshToken(token: String) {
        sharedPreferences.edit(commit = true) {
            putString(KEY_REFRESH_TOKEN, token)
        }
    }

    override fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    override fun saveUserRole(role: String) {
        sharedPreferences.edit(commit = true) {
            putString(KEY_USER_ROLE, role)
        }
    }

    override fun clearTokens() {
        sharedPreferences.edit(commit = true) {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
        }
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "jwt_access_token"
        private const val KEY_REFRESH_TOKEN = "jwt_refresh_token"
        private const val KEY_USER_ROLE = "user_role"
    }
}
