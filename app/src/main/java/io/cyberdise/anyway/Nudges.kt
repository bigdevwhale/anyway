package io.cyberdise.anyway

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.random.Random

/** One "you're still alive" notification a day, at a random time between 10:00 and 21:00. */
object Nudges {
    private const val CHANNEL_ID = "nudges"
    private const val NOTIFICATION_ID = 1
    private val WINDOW_START = LocalTime.of(10, 0)
    private val WINDOW_END = LocalTime.of(21, 0)

    fun schedule(context: Context) {
        val store = Store(context)
        val alarms = context.getSystemService(AlarmManager::class.java)
        val pending = alarmIntent(context)
        alarms.cancel(pending)
        if (!store.nudgesEnabled || store.birthDate == null) {
            store.nextNudgeAt = 0
            return
        }

        val now = ZonedDateTime.now()
        // Keep an already-planned time so reopening the app doesn't reshuffle it.
        val at = store.nextNudgeAt.takeIf { it > now.toInstant().toEpochMilli() }
            ?: nextMoment(now, alreadyShownToday = store.lastNudge == now.toLocalDate())
        store.nextNudgeAt = at
        alarms.set(AlarmManager.RTC_WAKEUP, at, pending)
    }

    private fun nextMoment(now: ZonedDateTime, alreadyShownToday: Boolean): Long {
        var day: LocalDate = now.toLocalDate()
        var from = maxOf(WINDOW_START, now.toLocalTime().plusMinutes(1))
        if (alreadyShownToday || from >= WINDOW_END) {
            day = day.plusDays(1)
            from = WINDOW_START
        }
        val span = WINDOW_END.toSecondOfDay() - from.toSecondOfDay()
        val time = from.plusSeconds(Random.nextLong(span.toLong()))
        return day.atTime(time).atZone(now.zone).toInstant().toEpochMilli()
    }

    fun show(appContext: Context) {
        val context = Locales.wrap(appContext)
        val store = Store(context)
        store.lastNudge = LocalDate.now()
        store.nextNudgeAt = 0

        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.nudge_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        )
        val text = context.resources.getStringArray(R.array.nudges).random()
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_anyway)
            .setColor(context.getColor(R.color.ember))
            .setContentTitle(context.getString(R.string.nudge_title))
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun alarmIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, NudgeReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}

class NudgeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Nudges.show(context)
        Nudges.schedule(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) Nudges.schedule(context)
    }
}
