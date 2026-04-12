package com.om.timely.auth.data.mappers

import androidx.compose.ui.text.toUpperCase
import com.om.timely.auth.data.dto.AuthResultDto
import com.om.timely.auth.data.dto.CreateUserResultDto
import com.om.timely.auth.data.dto.UserOutDto
import com.om.timely.auth.domain.AuthResult
import com.om.timely.auth.domain.User
import com.om.timely.auth.domain.UserRole
import kotlin.concurrent.thread

fun CreateUserResultDto.toDomain(): User {
    return User(
        name = this.name,
        email = this.email,
        // Assuming your backend sends "STUDENT" or "FACULTY"
        role = UserRole.valueOf(this.role.uppercase()),
        dept = this.dept,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun AuthResultDto.toDomain(): AuthResult {
    return AuthResult(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken,
        role = UserRole.valueOf(this.role.toUpperCase())
    )
}
fun UserOutDto.toDomain(): User {
    return User(
        name = this.name,
        email = this.email,
        // Assuming
        role = UserRole.valueOf(this.role.toUpperCase()),
        dept = dept,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}