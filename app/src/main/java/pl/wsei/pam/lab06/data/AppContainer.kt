package pl.wsei.pam.lab06.data

import android.content.Context

interface AppContainer {

    val todoTaskRepository: TodoTaskRepository
    val currentDateProvider: CurrentDateProvider
}