package org.bikerent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bikerent.api.RetrofitClient
import org.bikerent.api.model.Bike
import org.bikerent.api.model.BikeStatus
import org.bikerent.databinding.ActivityBikeRentingBinding
import org.nosemaj.kosmos.Tokens
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BikeRentingActivity : AppCompatActivity() {
    private lateinit var view: ActivityBikeRentingBinding
    private val auth get() = (applicationContext as BikeRentApp).auth

    var bikeLocation = R.string.empty.toString()
    var bikeId = R.string.empty.toString()
    var bikeManufacturer = R.string.empty.toString()
    var username = R.string.empty.toString()

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
        view.backButton.isEnabled = false
        view.backButton.setOnClickListener {
            goToShowBikesPage(this@BikeRentingActivity,username, bikeLocation)
        }
        navigate()
    }

    private fun finishRenting() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> rentBike(token.idToken, bikeId)
                else -> goToSignIn(source = this@BikeRentingActivity)
            }
        }
    }


    private fun rentBike(token: String, bikeId: String) {
        val bikeStatus = BikeStatus(bikeId, false)

        val call = RetrofitClient.getInstance().service.updateBike(token, bikeStatus)

        call.enqueue(object : Callback<BikeStatus?> {
            override fun onResponse(
                call: Call<BikeStatus?>,
                response: Response<BikeStatus?>
            ) {
                if(response.code() in 200..299) {
                    goToShowBikesPage(this@BikeRentingActivity, username, bikeLocation)
                } else {
                    displayMessage("Failed to rent bike " + response.code())
                }
            }

            override fun onFailure(call: Call<BikeStatus?>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
            }
        })

    }

    private fun navigate() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> displayBikeRentingData(token.idToken)
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

    private fun displayBikeRentingData(token: String) {

        displayMessage(username + " has successfully rented bike " + bikeManufacturer)

    }

    private fun getProperty(propertyName: String): String {
        val extras = intent.extras
        if (extras != null) {
            return extras.getString(propertyName)!!
        }
        return R.string.empty.toString()
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
