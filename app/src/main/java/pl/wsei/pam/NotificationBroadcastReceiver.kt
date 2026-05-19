package pl.wsei.pam

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import pl.wsei.pam.lab01.R

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // 1. Wyświetlamy powiadomienie o zbliżającym się terminie
        val title = intent?.getStringExtra(titleExtra) ?: "Deadline"
        val message = intent?.getStringExtra(messageExtra) ?: "Zbliża się termin zakończenia zadania"

        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)

        // 2. Automatyczne powtarzanie co 4 godziny
        val nextTriggerTime = intent?.getLongExtra("NEXT_TRIGGER_TIME", 0L) ?: 0L

        // Planujemy kolejne powtórzenie tylko, jeśli czas nie wybiega dziwnie w przeszłość
        if (nextTriggerTime > System.currentTimeMillis()) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val nextIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
                putExtra(titleExtra, title)
                putExtra(messageExtra, message)
                // Ustawiamy znacznik dla jeszcze kolejnego powtórzenia (za następne 4 godziny)
                putExtra("NEXT_TRIGGER_TIME", nextTriggerTime + (4 * 60 * 60 * 1000))
            }

            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationID,
                nextIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                nextPendingIntent
            )
        }
    }
}