package com.om.timely.core.presentation.util

fun String.toDepartmentAbbreviation(): String {
    return when (this.trim().lowercase()) {
        "computer science and engineering", "computer science & engineering", "computer science" -> "CSE"
        "information technology" -> "IT"
        "electronics and communication engineering", "electronics & communication engineering" -> "ECE"
        "electrical engineering" -> "EE"
        "electrical and electronics engineering", "electrical & electronics engineering" -> "EEE"
        "mechanical engineering" -> "ME"
        "civil engineering" -> "CE"
        "artificial intelligence and data science", "artificial intelligence & data science" -> "AIDS"
        "artificial intelligence and machine learning", "artificial intelligence & machine learning" -> "AIML"
        else -> {
            // Smart Fallback: Takes the first letter of each word, ignoring "and" or "&"
            this.split(Regex("\\s+"))
                .filter {
                    it.isNotEmpty() &&
                            !it.equals("and", ignoreCase = true) &&
                            !it.equals("of", ignoreCase = true) &&
                            it != "&"
                }
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
        }
    }
}