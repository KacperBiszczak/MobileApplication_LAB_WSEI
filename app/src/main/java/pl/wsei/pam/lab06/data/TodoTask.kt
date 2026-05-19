package pl.wsei.pam.lab06.data

import java.time.LocalDate

data class TodoTask(
    val id: Int,
    val title: String,
    val deadline: LocalDate,
    var isDone: Boolean,
    val priority: Priority
)
