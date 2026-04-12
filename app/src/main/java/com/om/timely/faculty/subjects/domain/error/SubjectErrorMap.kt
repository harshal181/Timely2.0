package com.om.timely.faculty.subjects.domain.error

import com.om.timely.R
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.presentation.UiText
import com.om.timely.core.presentation.asUiText

fun SubjectError.asUiText(): UiText {
    return when(this) {
        SubjectError.Local.SUBJECT_NOT_FOUND -> UiText.DynamicString("Subject not found or does not exist.")
        SubjectError.Local.CONFLICT -> UiText.DynamicString("A subject with this code already exists.")
        SubjectError.Local.UNKNOWN_ERROR -> UiText.DynamicString("An unknown error occurred.")
        is SubjectError.Network -> this.networkError.asUiText()
    }
}


