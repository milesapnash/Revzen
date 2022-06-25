package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.*
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.PetResponse

const val SECSTOMILLIS = 1000

class StudyActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private lateinit var apiHandler: ApiHandler
    private lateinit var timeTracker: SessionData
    private var studyLength = 60
    private var breakLength = 10
    private var studyList = ArrayList<SessionData>()
    private var inSession = true
    private var validLeave = false
    private var originalTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        studyLength = intent.extras?.getInt("studyLength")!!
        breakLength = intent.extras?.getInt("breakLength")!!
        studyList = intent.extras?.getParcelableArrayList("studyList")!!

        // updating getting the api handler
        apiHandler = intent.extras?.getParcelable("handler")!!
        timeTracker = intent.extras?.getParcelable("timeTracker")!!

        //api request to get main pet
        apiHandler.getPetInfo(this::successGet, this::failGet)

        timer = findViewById(R.id.chronometer)
        originalTime = SystemClock.elapsedRealtime()
        timer.base = originalTime + (studyLength * SECSTOMILLIS).toLong()
        timer.onChronometerTickListener = this
        timer.start()
    }

    private fun getElapsedTime(): Int =
        ((SystemClock.elapsedRealtime() - originalTime) / 1000).toInt()

    private fun updateTimeTracker() {
        timeTracker.study_time += getElapsedTime()
    }

    override fun onUserLeaveHint() {
        if (!validLeave) {
            super.onUserLeaveHint()
            timer.stop()
            updateTimeTracker()

            startActivity(Intent(this, FailActivity::class.java).apply {
                putExtra("reason", "leaveApp")
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                studyList.add(timeTracker)
                putExtra("studyList", studyList)
            })

            finish()
        }
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    override fun onChronometerTick(chronometer: Chronometer) {
        val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if (elapsedMillis == 0L) {
            chronometer.base -= (1000)
        }
        if ((elapsedMillis <= 0) && inSession) {
            setBreakView()
            inSession = false
        } else if ((elapsedMillis > 0) && !inSession) {
            setTimerView()
            inSession = true
        } else if (elapsedMillis < -30 * SECSTOMILLIS) {
            timer.stop()
            goToFail()
        } else if (elapsedMillis < -20 * SECSTOMILLIS) {
            setWarningView()
        }
    }

    private fun setBreakView() {
        findViewById<TextView>(R.id.studyTitleText).text = resources.getString(R.string.break_title)
        findViewById<TextView>(R.id.warningView).visibility = View.INVISIBLE
        findViewById<Button>(R.id.endSessionButton).text =
            resources.getString(R.string.break_button)
    }

    private fun setTimerView() {
        findViewById<TextView>(R.id.studyTitleText).text =
            resources.getString(R.string.session_title)
        findViewById<TextView>(R.id.warningView).visibility = View.VISIBLE
        findViewById<Button>(R.id.endSessionButton).text =
            resources.getString(R.string.end_session_button)
    }

    private fun setWarningView() {
        findViewById<TextView>(R.id.warningView).text = resources.getString(R.string.warning_title2)
        findViewById<TextView>(R.id.warningView).visibility = View.VISIBLE
    }

    private fun goToFail(){
        validLeave = true
        startActivity(Intent(this, FailActivity::class.java).apply {
            putExtra("reason", "studyTimeout")
            putExtra("handler", apiHandler)
            putExtra("timeTracker", timeTracker)
            studyList.add(timeTracker)
            putExtra("studyList", studyList)
        })

        finish()
    }

    fun goToEndSession(_view: View) {
        validLeave = true
        timer.stop()
        updateTimeTracker()

        startActivity(if (inSession) {
            Intent(this, FailActivity::class.java).apply {
                putExtra("reason", "giveUp")
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                studyList.add(timeTracker)
                putExtra("studyList", studyList)
            }
        } else {
            Intent(this, BreakActivity::class.java).apply {
                putExtra("breakLength", breakLength)
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                putExtra("studyList", studyList)
            }
        })
        finish()
    }

    private fun successGet(info: PetResponse) {
        findViewById<ImageView>(R.id.petView).setImageResource(info.selectedPet.studyImage)
        findViewById<ImageView>(R.id.petView).visibility = View.VISIBLE
    }

    private fun failGet(error: ApiError) {
        findViewById<ImageView>(R.id.petView).visibility = View.INVISIBLE
    }
}