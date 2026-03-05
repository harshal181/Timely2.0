package com.om.timely.navigation

sealed class Screen(val route: String) {
    sealed class Auth(route: String): Screen(route) {
        object Login: Auth("login")
        object Signup: Auth("signup")
        object ForgotPassword: Auth("forgot_password")

    }
    sealed class Faculty(route: String): Screen(route) {
        object Dashboard : Faculty("faculty_dashboard")
        object ManageCourses : Faculty("manage_courses")
        object StudentList : Faculty("student_list")
        // Example with arguments
        object Profile : Faculty("faculty_profile/{facultyId}") {
            fun passId(id: String) = "faculty_profile/$id"
        }
    }
    // Student Graph
    sealed class Student(route: String) : Screen(route) {
        object Home : Student("student_home")
        object EnrolledCourses : Student("enrolled_courses")
        object Grades : Student("grades")
    }

    // Root Graph Identifiers (Optional but helpful for nesting)
    object AuthGraph : Screen("auth_graph")
    object FacultyGraph : Screen("faculty_graph")
    object StudentGraph : Screen("student_graph")

    object Splash: Screen("splash")
}