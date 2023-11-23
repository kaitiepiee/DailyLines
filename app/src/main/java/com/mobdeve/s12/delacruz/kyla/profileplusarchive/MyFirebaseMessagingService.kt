package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelID = "notification_channel"
const val channelName = "DailyLines"

/**
 * This class allows for push notifications using Firebase Cloud Messaging (FCM).
 * There is a daily notification scheduled at 11:00AM to remind the user to write
 * a daily journal.
 * Devs who have access may schedule notifications via this link:
 * https://console.firebase.google.com/u/0/project/mobdeve-s12-dailylines-ba62a/messaging
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.getNotification() != null){
            generateNotification(remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!!)
        }
    }
    fun getRemoteView(title: String, message: String): RemoteViews? {
        val remoteViews = RemoteViews("com.mobdeve.s12.delacruz.kyla.profileplusarchive", R.layout.notification)
        remoteViews.setTextViewText(R.id.title, title)
        remoteViews.setTextViewText(R.id.message, message)
        remoteViews.setImageViewResource(R.id.app_logo, R.drawable.best_mood_icon)
        return remoteViews
    }
    fun generateNotification(title: String, message: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, channelID)
            .setSmallIcon(R.drawable.best_mood_icon) // app logo
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000)) // for notif vibration
            .setOnlyAlertOnce(true) // can be turned off for testing
            .setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(title,message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)

        notificationManager.notify(0, builder.build())
    }
}

/**
source: https://www.youtube.com/watch?v=2xoJi-ZHmNI
 **/