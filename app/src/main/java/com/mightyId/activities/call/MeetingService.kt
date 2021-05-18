package com.mightyId.activities.call

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mightyId.R
import com.mightyId.models.RequestCall
import com.mightyId.utils.Constant.Companion.MEETING_ID
import com.mightyId.utils.Constant.Companion.MEETING_IN_PROGRESS
import com.mightyId.utils.Constant.Companion.CALL_REQUEST
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.buildChannel
import com.mightyId.utils.loadImageToNotificationAvatar
import org.jetbrains.anko.notificationManager
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class MeetingService : Service() {

    @ExperimentalTime
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.getInfoExtra<RequestCall>(CALL_REQUEST)
        val notificationIntent = Intent(this, MeetingActivity::class.java)
        val avatar = loadImageToNotificationAvatar(data?.callerPhotoURL)
        val pendingIntent = PendingIntent.getActivity(this, 100, notificationIntent, 0)
        var meetingDuration: Long = 0
        val builder = NotificationCompat.Builder(this, MEETING_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("WorkId Meeting")
            .setLargeIcon(avatar)
            .setContentText(meetingDuration.toDuration(DurationUnit.SECONDS).toString())
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
        val notification = builder.build()
        val state = object : CountDownTimer(0, -1000) {
            override fun onTick(millisUntilFinished: Long) {
                meetingDuration = millisUntilFinished
                notificationManager.notify(MEETING_IN_PROGRESS, notification)
            }

            override fun onFinish() {
            }
        }
        with(notificationManager) {
            buildChannel(MEETING_ID,
                MEETING_ID,
                NotificationManager.IMPORTANCE_HIGH)
            state.start()
            startForeground(MEETING_IN_PROGRESS, notification)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}