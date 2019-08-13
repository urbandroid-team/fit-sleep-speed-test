package com.urbandroid.googlefitspeed


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions.*
import com.google.android.gms.fitness.data.DataType.TYPE_ACTIVITY_SEGMENT
import com.google.android.gms.fitness.request.SessionReadRequest
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.MILLISECONDS

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fitnessOptions = builder()
            .addDataType(TYPE_ACTIVITY_SEGMENT, ACCESS_READ)
            .addDataType(TYPE_ACTIVITY_SEGMENT, ACCESS_WRITE)
            .build()

        val hasPermissions = GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this@MainActivity), fitnessOptions)
        if (!hasPermissions) {
            GoogleSignIn.requestPermissions(
                this@MainActivity,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this@MainActivity),
                fitnessOptions
            )
        } else {
            readButton.isEnabled = true
        }

        readButton.setOnClickListener {
            readButton.isEnabled = false
            try { readSleepRecords() } finally { readButton.isEnabled = true }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            readButton.isEnabled = true
        }
    }

    private fun readSleepRecords() {
        fun prepareRequest(from: Long, to: Long) = SessionReadRequest.Builder()
            .setTimeInterval(from, to, MILLISECONDS)
            .read(TYPE_ACTIVITY_SEGMENT)
            .readSessionsFromAllApps()
            .enableServerQueries()
            .build()

        fun call(account: GoogleSignInAccount, from: Long, to: Long, countDown: Int) {
            if (countDown == 0) return
            val readRequest = prepareRequest(from, to)
            val t = now()
            Fitness.getSessionsClient(this, account)
                .readSession(readRequest)
                .addOnSuccessListener { response ->
                    val sessions = response.sessions
                    info("Read ${sessions.size} sessions [${from.prettyDate} - ${to.prettyDate}] took ${now() - t} ms")
                    call(account, from - DAYS.toMillis(14), from, countDown - 1)
                }
                .addOnFailureListener { info("Failed to read session $it") }
        }

        val to = now()
        val from = to - DAYS.toMillis(14)

        GoogleSignIn.getLastSignedInAccount(this)?.let {
            call(it, from, to, 10)
        }
    }

    companion object {
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 10000
    }
}

/*
marcel.matula@gmail.com
Test I
2019-08-13 15:40:23.759 I: Read 14 sessions [2019-07-30 15:40 - 2019-08-13 15:40] took 2160 ms
2019-08-13 15:40:28.513 I: Read 15 sessions [2019-07-16 15:40 - 2019-07-30 15:40] took 4754 ms
2019-08-13 15:40:33.708 I: Read 13 sessions [2019-07-02 15:40 - 2019-07-16 15:40] took 5194 ms
2019-08-13 15:40:38.394 I: Read 13 sessions [2019-06-18 15:40 - 2019-07-02 15:40] took 4685 ms
2019-08-13 15:40:38.895 I: Read 1 sessions [2019-06-04 15:40 - 2019-06-18 15:40] took 501 ms
2019-08-13 15:40:46.292 I: Read 13 sessions [2019-05-21 15:40 - 2019-06-04 15:40] took 7396 ms
2019-08-13 15:40:55.633 I: Read 14 sessions [2019-05-07 15:40 - 2019-05-21 15:40] took 9340 ms
2019-08-13 15:41:04.125 I: Read 13 sessions [2019-04-23 15:40 - 2019-05-07 15:40] took 8491 ms
2019-08-13 15:41:13.630 I: Read 14 sessions [2019-04-09 15:40 - 2019-04-23 15:40] took 9504 ms
2019-08-13 15:41:23.185 I: Read 14 sessions [2019-03-26 14:40 - 2019-04-09 15:40] took 9555 ms

Test II
2019-08-13 15:42:53.627 I: Read 14 sessions [2019-07-30 15:42 - 2019-08-13 15:42] took 1974 ms
2019-08-13 15:42:58.305 I: Read 15 sessions [2019-07-16 15:42 - 2019-07-30 15:42] took 4677 ms
2019-08-13 15:43:02.553 I: Read 13 sessions [2019-07-02 15:42 - 2019-07-16 15:42] took 4247 ms
2019-08-13 15:43:07.388 I: Read 13 sessions [2019-06-18 15:42 - 2019-07-02 15:42] took 4834 ms
2019-08-13 15:43:07.827 I: Read 1 sessions [2019-06-04 15:42 - 2019-06-18 15:42] took 438 ms
2019-08-13 15:43:15.277 I: Read 13 sessions [2019-05-21 15:42 - 2019-06-04 15:42] took 7450 ms
2019-08-13 15:43:24.596 I: Read 14 sessions [2019-05-07 15:42 - 2019-05-21 15:42] took 9318 ms
2019-08-13 15:43:33.154 I: Read 13 sessions [2019-04-23 15:42 - 2019-05-07 15:42] took 8557 ms
2019-08-13 15:43:42.564 I: Read 14 sessions [2019-04-09 15:42 - 2019-04-23 15:42] took 9409 ms
2019-08-13 15:43:52.067 I: Read 14 sessions [2019-03-26 14:42 - 2019-04-09 15:42] took 9502 ms

Test III
2019-08-13 15:44:36.061 I: Read 14 sessions [2019-07-30 15:44 - 2019-08-13 15:44] took 1767 ms
2019-08-13 15:44:40.524 I: Read 15 sessions [2019-07-16 15:44 - 2019-07-30 15:44] took 4462 ms
2019-08-13 15:44:45.139 I: Read 13 sessions [2019-07-02 15:44 - 2019-07-16 15:44] took 4614 ms
2019-08-13 15:44:49.421 I: Read 13 sessions [2019-06-18 15:44 - 2019-07-02 15:44] took 4280 ms
2019-08-13 15:44:49.845 I: Read 1 sessions [2019-06-04 15:44 - 2019-06-18 15:44] took 424 ms
2019-08-13 15:44:56.475 I: Read 13 sessions [2019-05-21 15:44 - 2019-06-04 15:44] took 6629 ms
2019-08-13 15:45:05.407 I: Read 14 sessions [2019-05-07 15:44 - 2019-05-21 15:44] took 8931 ms
2019-08-13 15:45:13.229 I: Read 13 sessions [2019-04-23 15:44 - 2019-05-07 15:44] took 7822 ms
2019-08-13 15:45:22.685 I: Read 14 sessions [2019-04-09 15:44 - 2019-04-23 15:44] took 9455 ms
2019-08-13 15:45:31.729 I: Read 14 sessions [2019-03-26 14:44 - 2019-04-09 15:44] took 9043 ms

fitsleepuser@gmail.com
Test I
2019-08-13 15:46:45.480 I: Read 13 sessions [2019-07-30 15:46 - 2019-08-13 15:46 took 202 ms
2019-08-13 15:46:45.639 I: Read 13 sessions [2019-07-16 15:46 - 2019-07-30 15:46 took 159 ms
2019-08-13 15:46:45.745 I: Read 12 sessions [2019-07-02 15:46 - 2019-07-16 15:46 took 106 ms
2019-08-13 15:46:45.852 I: Read 13 sessions [2019-06-18 15:46 - 2019-07-02 15:46 took 107 ms
2019-08-13 15:46:45.964 I: Read 1 sessions [2019-06-04 15:46 - 2019-06-18 15:46 took 112 ms
2019-08-13 15:46:46.133 I: Read 13 sessions [2019-05-21 15:46 - 2019-06-04 15:46 took 168 ms
2019-08-13 15:46:46.322 I: Read 14 sessions [2019-05-07 15:46 - 2019-05-21 15:46 took 189 ms
2019-08-13 15:46:46.474 I: Read 13 sessions [2019-04-23 15:46 - 2019-05-07 15:46 took 151 ms
2019-08-13 15:46:46.691 I: Read 14 sessions [2019-04-09 15:46 - 2019-04-23 15:46 took 216 ms
2019-08-13 15:46:46.837 I: Read 14 sessions [2019-03-26 14:46 - 2019-04-09 15:46 took 144 ms

Test II
2019-08-13 15:48:12.572 I: Read 13 sessions [2019-07-30 15:48 - 2019-08-13 15:48 took 138 ms
2019-08-13 15:48:12.686 I: Read 13 sessions [2019-07-16 15:48 - 2019-07-30 15:48 took 114 ms
2019-08-13 15:48:12.836 I: Read 12 sessions [2019-07-02 15:48 - 2019-07-16 15:48 took 149 ms
2019-08-13 15:48:12.943 I: Read 13 sessions [2019-06-18 15:48 - 2019-07-02 15:48 took 107 ms
2019-08-13 15:48:13.003 I: Read 1 sessions [2019-06-04 15:48 - 2019-06-18 15:48 took 60 ms
2019-08-13 15:48:13.146 I: Read 13 sessions [2019-05-21 15:48 - 2019-06-04 15:48 took 143 ms
2019-08-13 15:48:13.305 I: Read 14 sessions [2019-05-07 15:48 - 2019-05-21 15:48 took 158 ms
2019-08-13 15:48:13.472 I: Read 13 sessions [2019-04-23 15:48 - 2019-05-07 15:48 took 166 ms
2019-08-13 15:48:13.623 I: Read 14 sessions [2019-04-09 15:48 - 2019-04-23 15:48 took 151 ms
2019-08-13 15:48:13.802 I: Read 14 sessions [2019-03-26 14:48 - 2019-04-09 15:48 took 179 ms

Test III
2019-08-13 15:48:33.326 I: Read 13 sessions [2019-07-30 15:48 - 2019-08-13 15:48 took 144 ms
2019-08-13 15:48:33.454 I: Read 13 sessions [2019-07-16 15:48 - 2019-07-30 15:48 took 128 ms
2019-08-13 15:48:33.592 I: Read 12 sessions [2019-07-02 15:48 - 2019-07-16 15:48 took 138 ms
2019-08-13 15:48:33.726 I: Read 13 sessions [2019-06-18 15:48 - 2019-07-02 15:48 took 134 ms
2019-08-13 15:48:33.829 I: Read 1 sessions [2019-06-04 15:48 - 2019-06-18 15:48 took 103 ms
2019-08-13 15:48:34.076 I: Read 13 sessions [2019-05-21 15:48 - 2019-06-04 15:48 took 246 ms
2019-08-13 15:48:34.273 I: Read 14 sessions [2019-05-07 15:48 - 2019-05-21 15:48 took 196 ms
2019-08-13 15:48:34.498 I: Read 13 sessions [2019-04-23 15:48 - 2019-05-07 15:48 took 225 ms
2019-08-13 15:48:34.677 I: Read 14 sessions [2019-04-09 15:48 - 2019-04-23 15:48 took 179 ms
2019-08-13 15:48:34.865 I: Read 14 sessions [2019-03-26 14:48 - 2019-04-09 15:48 took 188 ms

 */