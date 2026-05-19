package pl.wsei.pam

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.ZoneId
import pl.wsei.pam.lab01.Lab01Activity
import pl.wsei.pam.lab01.R
import pl.wsei.pam.lab02.Lab02Activity
import pl.wsei.pam.lab03.Lab03Activity
import pl.wsei.pam.lab06.MainScreen
import pl.wsei.pam.lab06.TodoApplication
import pl.wsei.pam.lab06.data.AppContainer
import kotlin.collections.filter
import pl.wsei.pam.lab06.MainActivity as Lab06Activity

const val notificationID = 121
const val channelID = "Lab06 channel"
const val titleExtra = "title"
const val messageExtra = "message"

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent().apply {
                action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            }
            startActivity(intent)
        }

        createNotificationChannel()
        container = (this.application as TodoApplication).container
        checkAndSetupClosestAlarm()
//        scheduleAlarm(System.currentTimeMillis() + 5000)


        setContent {
            MainScreen()
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun onClickMainBtnRunLab01(v: View){
        val intent = Intent(this, Lab01Activity::class.java)
        startActivity(intent)
    }
    fun onClickMainBtnRunLab02(v: View){
        val intent = Intent(this, Lab02Activity::class.java)
        startActivity(intent)
    }
    fun onClickMainBtnRunLab06(v: View){
        val intent = Intent(this, Lab06Activity::class.java)
        startActivity(intent)
    }

    private fun createNotificationChannel() {

        val name = "Lab06 channel"

        val descriptionText =
            "Lab06 is channel for notifications for approaching tasks."

        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(
            channelID,
            name,
            importance
        ).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        lateinit var container: AppContainer
    }

    fun cancelAlarm() {
        val intent = Intent(applicationContext, NotificationBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE // FLAG_NO_CREATE sprawdza czy istnieje
        )

        // Jeśli taki alarm już istnieje w systemie – kasujemy go
        if (pendingIntent != null) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleAlarm(triggerTimeMillis: Long) {
        // Przed ustawieniem nowego alarmu, zawsze najpierw kasujemy poprzedni (wymóg zadania!)
        cancelAlarm()

        val intent = Intent(applicationContext, NotificationBroadcastReceiver::class.java).apply {
            putExtra(titleExtra, "Zbliża się termin zadania!")
            putExtra(messageExtra, "Masz niewykonane zadanie, którego deadline upływa za 24 godziny.")
            // Przekazujemy czas kolejnego wywołania (za 4 godziny) do odbiorcy
            putExtra("NEXT_TRIGGER_TIME", triggerTimeMillis + (4 * 60 * 60 * 1000))
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // setExactAndAllowWhileIdle gwarantuje, że alarm odpali się precyzyjnie co do sekundy
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
            pendingIntent
        )
    }

    fun checkAndSetupClosestAlarm() {
        lifecycleScope.launch {
            val allTasks = container.todoTaskRepository.getAllAsStream().firstOrNull() ?: emptyList()

            // 1. Wybieramy strefę czasową Twojego telefonu (domyślną dla systemu)
            val zoneId = ZoneId.systemDefault()

            val closestTask = allTasks
                .filter {
                    // Konwertujemy it.deadline na milisekundy typu Long
                    val taskTimeMillis = it.deadline.atStartOfDay(zoneId).toInstant().toEpochMilli()

                    // Filtrujemy niewykonane zadania, których czas jest w przyszłości
                    !it.isDone && taskTimeMillis > System.currentTimeMillis()
                }
                .minByOrNull {
                    // Sortujemy po przekonwertowanym czasie typu Long
                    it.deadline.atStartOfDay(zoneId).toInstant().toEpochMilli()
                }

            if (closestTask != null) {
                // Zamieniamy ostateczny deadline wybranego zadania na milisekundowy Long
                val closestTaskMillis = closestTask.deadline.atStartOfDay(zoneId).toInstant().toEpochMilli()

                // 2. Obliczamy czas alarmu: dokładnie 24 godziny wcześniej
                val alarmTime = closestTaskMillis - (24 * 60 * 60 * 1000)

                val finalAlarmTime = if (alarmTime < System.currentTimeMillis()) {
                    System.currentTimeMillis() + 5000 // za 5 sekund, jeśli zostało mniej niż doba
                } else {
                    alarmTime
                }

                scheduleAlarm(finalAlarmTime)
            } else {
                cancelAlarm()
            }
        }
    }
}


