package com.om.timely.core.domain.util

// Error Types Enum for Network
enum class NetworkError: Error {
    REQUEST_TIMEOUT,
    TOO_MANY_REQUEST,
    NO_INTERNET,
    CONFLICT,
    SERVER_ERROR,
    SERIALIZATION_ERROR,
    UNKNOWN,
    UNAUTHORIZED,
    BAD_REQ,
    NOT_FOUND,
    BAD_REQUEST
}

