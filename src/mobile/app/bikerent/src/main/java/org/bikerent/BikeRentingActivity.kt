package org.bikerent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bikerent.api.RetrofitClient
import org.bikerent.api.model.Bike
import org.bikerent.api.model.BikeUsed
import org.bikerent.databinding.ActivityBikeRentingBinding
import org.nosemaj.kosmos.Tokens
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit


class BikeRentingActivity : AppCompatActivity() {
    private lateinit var view: ActivityBikeRentingBinding
    private val auth get() = (applicationContext as BikeRentApp).auth

    var bikeLocation = R.string.empty.toString()
    var bikeId = R.string.empty.toString()
    var bikeManufacturer = R.string.empty.toString()
    var username = R.string.empty.toString()

    private val mInterval = 1
    private var mHandler: Handler? = null
    private var timeInSeconds = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityBikeRentingBinding.inflate(layoutInflater)
        setContentView(view.root)

        bikeLocation = getProperty(R.string.location.toString())
        bikeId = getProperty(R.string.bike_id.toString())
        bikeManufacturer = getProperty(R.string.bike_manufacturer.toString())
        username = getProperty(R.string.username.toString())

        view.finishButton.setOnClickListener {
            finishRenting()
        }
        navigate()
        initStopWatch()
    }

    private fun finishRenting() {
        stopTimer()
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> rentBike(token.idToken, bikeId)
                else -> goToSignIn(source = this@BikeRentingActivity)
            }
        }
    }

    private fun rentBike(token: String, bikeId: String) {
        val bikeStatus = BikeUsed(bikeId, false)

        val call = RetrofitClient.getInstance().service.updateBike(token, bikeStatus)

        call.enqueue(object : Callback<BikeUsed?> {
            override fun onResponse(
                call: Call<BikeUsed?>,
                response: Response<BikeUsed?>
            ) {
                if(response.code() in 200..299) {
                    goToShowBikesPage(this@BikeRentingActivity, username, true, bikeLocation)
                } else {
                    displayMessage("Failed to rent bike " + response.code())
                }
            }

            override fun onFailure(call: Call<BikeUsed?>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
            }
        })

    }

    private fun navigate() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> displayBikeRentingData()
                else -> goToSignIn(source = this@BikeRentingActivity)
            }
        }
    }

    private fun displayMessage(text: String?) {
        if (text != null) {
            view.message.text = text
            view.message.visibility = VISIBLE
        } else {
            view.message.visibility = INVISIBLE
        }
    }

    private fun displayBikeRentingData() {
        displayMessage("Successfully rented bike $bikeManufacturer\nClick finish once you are done")
    }

    private fun getProperty(propertyName: String): String {
        val extras = intent.extras
        if (extras != null) {
            return extras.getString(propertyName)!!
        }
        return R.string.empty.toString()
    }

    private fun initStopWatch() {
        view.timer.text = "00:00:00"
        mHandler = Handler(Looper.getMainLooper())
        mStatusChecker.run()
    }

    private fun stopTimer() {
        mHandler?.removeCallbacks(mStatusChecker)
    }

    private fun updateStopWatchView(timeInSeconds: Long) {
        val formattedTime = getFormattedStopWatch((timeInSeconds * 1000))
        view.timer.text = formattedTime
    }

    fun getFormattedStopWatch(ms: Long): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
    }

    private var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                timeInSeconds += 1
                updateStopWatchView(timeInSeconds)
            } finally {
                mHandler!!.postDelayed(this, mInterval.toLong())
            }
        }
    }

}
fun goToBikeRentingPage(source: Activity, username: String? = null, bike: Bike) {
    val intent = Intent(source, BikeRentingActivity::class.java)
    intent.putExtra(R.string.username.toString(), username)
    intent.putExtra(R.string.bike_id.toString(), bike.id)
    intent.putExtra(R.string.location.toString(), bike.location)
    intent.putExtra(R.string.bike_manufacturer.toString(), bike.manufacturer)
    intent.putExtra(R.string.bike_year.toString(), bike.year)
    source.startActivity(intent)
}
