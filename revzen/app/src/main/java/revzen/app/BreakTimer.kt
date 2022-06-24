package revzen.app

import android.content.Context
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class BreakTimer(millisInFuture: Long, countDownInterval: Long, private val context: Context) : CountDownTimer(millisInFuture,
    countDownInterval) {
    private val CHANNELID = "BREAK_NOTIFICATION"
    private val notificationId = 1
    private var notified = false
    private var builder = NotificationCompat.Builder(context, CHANNELID)
        .setSmallIcon(R.drawable.notif_icon)
        .setContentTitle("Return to your pet!")
        .setContentText("You have to return within 5 minutes or you will break your session!")
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText("You have to return within 5 minutes or you will break your session!"))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setAutoCancel(true)

    override fun onTick(millisUntilFinished: Long) {
    }

    override fun onFinish() {
        // reminds user to go back to the app with a notification when break is over
        if (!notified) {
            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
            notified = true
        }
    }
}