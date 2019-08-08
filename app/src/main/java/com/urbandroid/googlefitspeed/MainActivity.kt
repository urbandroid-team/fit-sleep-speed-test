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
                    info("Read ${sessions.size} sessions [${from.prettyDate} - ${to.prettyDate} took ${now() - t} ms")
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