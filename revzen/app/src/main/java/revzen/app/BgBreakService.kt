package revzen.app

import android.app.Service
import android.content.Intent
import android.os.IBinder

class BgBreakService : Service() {
    private var breakLength = 5
    private lateinit var timer: BreakTimer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            breakLength = intent.extras?.getInt("breakLength")!!
        }
        timer = BreakTimer((breakLength * SECS_TO_MILLIS).toLong(), 1000, this, intent)
        timer.start()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        timer.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}