package org.bikerent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bikerent.api.RetrofitClient
import org.bikerent.api.model.Bike
import org.bikerent.api.model.BikeActive
import org.bikerent.api.model.BikeUsed
import org.bikerent.databinding.ActivityShowBikesBinding
import org.nosemaj.kosmos.Tokens
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ShowBikesActivity : AppCompatActivity() {
    private lateinit var view: ActivityShowBikesBinding
    private val auth get() = (applicationContext as BikeRentApp).auth

    var location = ""
    var username = ""
    var selectedBike: Bike? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityShowBikesBinding.inflate(layoutInflater)
        setContentView(view.root)

        val location = getSelectedLocation()
        if (location == null) {
            goToShowLocationsPage(this@ShowBikesActivity, getProvidedUsername())
        } else {
            this.location = location
        }

        this.username = getProvidedUsername()

        view.signOutButton.setOnClickListener {
            signOut()
        }
        view.rentButton.isEnabled = false
        view.rentButton.setOnClickListener {
            rent()
        }
        view.resetButton.isEnabled = false
        view.resetButton.setOnClickListener {
            reset()
        }
        view.backButton.setOnClickListener {
            goToShowLocationsPage(this@ShowBikesActivity, username)
        }
        navigate()
    }

    private fun signOut() {
        lifecycleScope.launch {
            auth.signOut()
            goToSignIn(source = this@ShowBikesActivity, username)
        }
    }

    private fun getProvidedUsername(): String {
        val extras = intent.extras
        if (extras != null) {
            if (extras.getString(R.string.username.toString()) != null) {
                return extras.getString(R.string.username.toString())!!
            }
        }
        return ""
    }

    private fun getSelectedLocation(): String? {
        val extras = intent.extras
        if (extras != null) {
            return extras.getString(R.string.location.toString())!!
        }
        return null
    }

    private fun rent() {
        lifecycleScope.launch {
            rentBike(selectedBike!!)
        }
    }

    private fun rentBike(selectedBike: Bike) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> rentBike(token.idToken, selectedBike)
                else -> goToSignIn(source = this@ShowBikesActivity)
            }
        }
    }

    private fun rentBike(token: String, selectedBike: Bike) {
        val bikeUsed = BikeUsed(selectedBike.id, true)
        val call = RetrofitClient.getInstance().service.updateBike(token, bikeUsed)

        call.enqueue(object : Callback<BikeUsed?> {
            override fun onResponse(
                call: Call<BikeUsed?>,
                response: Response<BikeUsed?>
            ) {
                if(response.code() in 200..299) {
                    goToBikeRentingPage(source = this@ShowBikesActivity, username,
                        selectedBike
                    )
                } else {
                    displayMessage("Failed to rent bike " + response.code())
                }
            }

            override fun onFailure(call: Call<BikeUsed?>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
                view.bikeList.visibility = INVISIBLE
            }
        })

    }

    private fun reset() {
        lifecycleScope.launch {
            displayMessage(null)
            selectedBike = null
            view.rentButton.isEnabled = false
            view.resetButton.isEnabled = false
            view.bikeList.clearChoices()
        }
    }

    private fun navigate() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> displayBikes(token.idToken)
                else -> goToSignIn(source = this@ShowBikesActivity)
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

    private fun displayBikes(token: String) {

        val call = RetrofitClient.getInstance().service.listAvailableBikes(token, location)

        call.enqueue(object : Callback<List<Bike?>?> {
            override fun onResponse(
                call: Call<List<Bike?>?>,
                response: Response<List<Bike?>?>
            ) {
                val arrayAdapter: ArrayAdapter<*>

                val bikeResult = response.body()
                if (bikeResult == null) {
                    displayMessage("An error has occurred while fetching bikes for $location")
                    return
                }

                val bikes = arrayOfNulls<Bike>(bikeResult.size)
                val bikesSummary = arrayOfNulls<String>(bikeResult.size)
                for (i in bikeResult.indices) {
                    val bike = bikeResult[i]
                    if (bike != null) {
                        bikesSummary[i] = bike.manufacturer + ":" + bike.year
                        bikes[i] = bike
                    }
                }
                val mListView = view.bikeList

                arrayAdapter = ArrayAdapter(
                    applicationContext,
                    android.R.layout.simple_list_item_1, bikesSummary
                )
                mListView.adapter = arrayAdapter
                mListView.setOnItemClickListener { parent, _, position, _ ->
                    selectedBike = bikes[position]
                    displayMessage( "Click Rent!")
                    view.rentButton.isEnabled = true
                    view.resetButton.isEnabled = true
                }
                view.message.visibility = INVISIBLE


            }

            override fun onFailure(call: Call<List<Bike?>?>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
                view.bikeList.visibility = INVISIBLE
            }
        })



    }
}

fun goToShowBikesPage(source: Activity, username: String? = null, stopSource: Boolean, location: String) {
    if (stopSource) {
        source.finish()
    }
    val intent = Intent(source, ShowBikesActivity::class.java)
    intent.putExtra(R.string.username.toString(), username)
    intent.putExtra(R.string.location.toString(), location)
    source.startActivity(intent)
}
