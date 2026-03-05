package com.om.timely.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.om.timely.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    @DrawableRes val iconRes: Int // Store the reference ID
) {
    // Shared
    data object Home : BottomNavItem("home", "Home", R.drawable.home_24)
    data object Alerts : BottomNavItem("alerts", "Alerts", R.drawable.bell_24)
    data object Profile : BottomNavItem("profile", "Profile", R.drawable.user_24)

    // Student specific
    data object Calendar : BottomNavItem("calendar", "Calendar", R.drawable.calendar_24)

    // Faculty specific
    data object Edit : BottomNavItem("edit", "Edit", R.drawable.pencil_24)
}

val studentBottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Calendar,
    BottomNavItem.Alerts,
    BottomNavItem.Profile
)

val facultyBottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Edit,
    BottomNavItem.Alerts,
    BottomNavItem.Profile
)
