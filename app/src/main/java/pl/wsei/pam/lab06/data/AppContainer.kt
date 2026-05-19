package pl.wsei.pam.lab06.data

import android.content.Context
import pl.wsei.pam.NotificationHandler

interface AppContainer {

    val todoTaskRepository: TodoTaskRepository
    val currentDateProvider: CurrentDateProvider
    val notificationHandler: NotificationHandler
}