package com.example.diary

import java.text.SimpleDateFormat
import java.util.*

data class Note(
    val id: String? = null,
    val title: String? = null,
    val content: String? = null,
    val timestamp: Long? = null
) {
    fun getFormattedDate(): String {
        timestamp?.let {
            val date = Date(it)
            val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return format.format(date)
        }
        return ""
    }
}