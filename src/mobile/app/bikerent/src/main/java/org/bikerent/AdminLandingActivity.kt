package org.bikerent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.auth0.android.jwt.JWT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bikerent.api.RetrofitClient
import org.bikerent.api.model.Bike
import org.bikerent.api.model.BikeActive
import org.bikerent.api.model.Message
import org.bikerent.databinding.ActivityAdminLandingBinding
import org.bikerent.databinding.ActivityShowLocationsBinding
import org.nosemaj.kosmos.Tokens
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdminLandingActivity : AppCompatActivity() {
    private lateinit var view: ActivityAdminLandingBinding
    private val auth get() = (applicationContext as BikeRentApp).auth
    private var selectedLocation = R.string.empty.toString()
    private var username = R.string.empty.toString()
    private var selectedBike: Bike? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityAdminLandingBinding.inflate(layoutInflater)
        setContentView(view.root)
        username = getProperty(R.string.username.toString())

        view.signOutButton.isEnabled = true
        view.signOutButton.setOnClickListener {
            view.signOutButton.isEnabled = false
            signOut()
        }

        view.createBikeButton.isEnabled = true
        view.createBikeButton.setOnClickListener {
            view.createBikeButton.isEnabled = false
            createBike()
        }

        view.updateButton.isEnabled = false
        view.updateButton.setOnClickListener {
            view.updateButton.isEnabled = false
            updateBike()
        }

        view.deleteButton.isEnabled = false
        view.deleteButton.setOnClickListener {
            view.deleteButton.isEnabled = false
            view.updateButton.isEnabled = false
            deleteBike()
        }

        loadLocations()
    }

    private fun signOut() {
        lifecycleScope.launch {
            auth.signOut()
            goToSignIn(source = this@AdminLandingActivity, username)
        }
    }

    private fun createBike() {
        goToAdminCreateBikePage(this@AdminLandingActivity, username)
    }

    private fun updateBike() {
        if (selectedBike?.isUsed == true) {
            displayMessage("Bike is currently used!")
        }
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> updateBike(token.idToken)
                else -> goToSignIn(source = this@AdminLandingActivity, username)
            }
        }
    }

    private fun updateBike(idToken: String) {
        if (selectedBike == null) {
            return
        }

        var bikeActive = BikeActive(selectedBike!!.id, !selectedBike!!.isActive)

        val call = RetrofitClient.getInstance().service.disableBike(idToken, bikeActive)

        call.enqueue(object : Callback<BikeActive> {
            override fun onResponse(call: Call<BikeActive>, response: Response<BikeActive>) {
                displayMessage("Bike updated!")
            }

            override fun onFailure(call: Call<BikeActive>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
            }})
        loadBikes()
    }

    private fun deleteBike() {
        if (selectedBike?.isUsed == true) {
            displayMessage("Bike is currently used!")
        }

        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> deleteBike(token.idToken)
                else -> goToSignIn(source = this@AdminLandingActivity, username)
            }
        }
    }

    private fun deleteBike(idToken: String) {
        val call = RetrofitClient.getInstance().service.deleteBike(idToken, selectedBike?.id ?: "")

        call.enqueue(object : Callback<Message> {
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                displayMessage("Bike removed")
            }

            override fun onFailure(call: Call<Message>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
            }})

        // TODO reload
        loadBikes()
    }

    private fun loadLocations() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> displayLocations(token.idToken)
                else -> goToSignIn(source = this@AdminLandingActivity, username)
            }
        }
    }

    private fun displayLocations(idToken: String) {
        val call = RetrofitClient.getInstance().service.listBikeLocations(idToken)
        call.enqueue(object : Callback<List<String?>?> {
            override fun onResponse(
                call: Call<List<String?>?>,
                response: Response<List<String?>?>
            ) {
                val arrayAdapter: ArrayAdapter<*>

                val locationsResult = response.body()
                if (locationsResult == null) {
                    displayMessage("An error has occurred while fetching locations")
                    return
                }

                val locations = arrayOfNulls<String>(locationsResult.size)
                for (i in locationsResult.indices) {
                    locations[i] = locationsResult.get(i)
                }
                val mListView = view.locationList

                arrayAdapter = ArrayAdapter(
                    applicationContext,
                    android.R.layout.simple_list_item_1, locations
                )
                mListView.adapter = arrayAdapter
                mListView.setOnItemClickListener { parent, _, position, _ ->
                    selectedLocation = parent.getItemAtPosition(position) as String
                    loadBikes()
                }
                view.message.visibility = INVISIBLE
            }

            override fun onFailure(call: Call<List<String?>?>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
                view.locationList.visibility = INVISIBLE
            }
        })
    }

    private fun loadBikes() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> displayBikes(token.idToken)
                else -> goToSignIn(source = this@AdminLandingActivity, username)
            }
        }
    }

    private fun displayBikes(token: String) {
        val call = RetrofitClient.getInstance().service.listAllBikes(token, selectedLocation)

        call.enqueue(object : Callback<List<Bike?>?> {
            override fun onResponse(
                call: Call<List<Bike?>?>,
                response: Response<List<Bike?>?>
            ) {
                val arrayAdapter: ArrayAdapter<*>

                val bikeResult = response.body()
                if (bikeResult == null) {
                    displayMessage("An error has occurred while fetching bikes for $selectedLocation")
                    return
                }

                if (bikeResult.size == 0) {
                    displayMessage("No bikes at location!")
                    return
                }

                val bikes = arrayOfNulls<Bike>(bikeResult.size)

                val bikesSummary = arrayOfNulls<String>(bikeResult.size)
                for (i in bikeResult.indices) {
                    val bike = bikeResult[i]
                    if (bike != null) {
                        bikesSummary[i] = bike.manufacturer + ":" + bike.year + " | active: " + bike.isActive + " | used: " + bike.isUsed
                        bikes[i] = bike
                    }
                }
                val mListView = view.bikeList

                arrayAdapter = ArrayAdapter(
                    applicationContext,
                    android.R.layout.simple_list_item_1, bikesSummary
                )
                mListView.adapter = arrayAdapter
                mListView.setOnItemClickListener { _, _, position, _ ->
                    selectedBike = bikes[position]
                    view.updateButton.isEnabled = true
                    view.deleteButton.isEnabled = true
                }
                view.message.visibility = INVISIBLE
            }

            override fun onFailure(call: Call<List<Bike?>?>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
                view.bikeList.visibility = INVISIBLE
            }
        })
    }

    private fun getProperty(propertyName: String): String {
        val extras = intent.extras
        if (extras != null) {
            return extras.getString(propertyName)!!
        }
        return R.string.empty.toString()
    }

    private fun displayMessage(text: String?) {
        if (text != null) {
            view.message.text = text
            view.message.visibility = VISIBLE
        } else {
            view.message.visibility = INVISIBLE
        }
    }

        }
fun goToAdminLandingPage(origin: Activity, username: String) {
    val intent = Intent(origin, AdminLandingActivity::class.java)
    intent.putExtra(R.string.username.toString(), username)
    origin.startActivity(intent)
}


