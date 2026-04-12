package com.om.timely.faculty.subjects.domain.error

import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.presentation.UiText
import com.om.timely.R
import com.om.timely.core.presentation.asUiText

//fun SubjectError.asUiText(): UiText {
//    return when (this) {
//        is SubjectError.Local -> {
//            when (this) {
//                SubjectError.Local.SUBJECT_NOT_FOUND -> UiText.StringResource(R.string.error_subject_not_found)
//                SubjectError.Local.CONFLICT -> UiText.StringResource(R.string.error_conflict)
//                SubjectError.Local.UNKNOWN_ERROR -> UiText.StringResource(R.string.error_unknown)
//            }
//        }
//
//        is SubjectError.Network -> this.networkError.asUiText()
//    }
//}
