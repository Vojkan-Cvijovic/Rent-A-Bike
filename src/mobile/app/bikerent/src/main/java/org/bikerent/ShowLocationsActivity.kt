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
import org.bikerent.databinding.ActivityShowLocationsBinding
import org.nosemaj.kosmos.Tokens
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ShowLocationsActivity : AppCompatActivity() {
    private lateinit var view: ActivityShowLocationsBinding
    private val auth get() = (applicationContext as BikeRentApp).auth
    private var selectedLocation = R.string.empty.toString()
    private var username = R.string.empty.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityShowLocationsBinding.inflate(layoutInflater)
        setContentView(view.root)
        view.signOutButton.setOnClickListener {
            signOut()
        }

        view.selectButton.isEnabled = false
        view.selectButton.setOnClickListener {
            select()
        }
        view.resetButton.isEnabled = false
        view.resetButton.setOnClickListener {
            reset()
        }

        username = getProperty(R.string.username.toString())

        navigate()
    }

    private fun signOut() {
        lifecycleScope.launch {
            auth.signOut()
            goToSignIn(source = this@ShowLocationsActivity, username)
        }
    }

    private fun select() {
        lifecycleScope.launch {
            displayMessage(null)
            goToShowBikesPage(source = this@ShowLocationsActivity, username, false, selectedLocation)
        }
    }

    private fun reset() {
        lifecycleScope.launch {
            displayMessage(null)
            // TODO still selected?
            view.locationList.setItemChecked(-1, true)
            selectedLocation = R.string.empty.toString()
            view.selectButton.isEnabled = false
            view.resetButton.isEnabled = false
        }
    }

    private fun navigate() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> displayLocations(token.idToken)
                else -> goToSignIn(source = this@ShowLocationsActivity, username)
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

    private fun displayLocations(token: String) {
        val call = RetrofitClient.getInstance().service.listBikeLocations(token)
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
                    //displayMessage( "Click Select to continue!")
                    view.selectButton.isEnabled = true
                    view.resetButton.isEnabled = true
                }
                view.message.visibility = INVISIBLE


            }

            override fun onFailure(call: Call<List<String?>?>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
                view.locationList.visibility = INVISIBLE
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
}

fun goToShowLocationsPage(origin: Activity, username: String) {
    val intent = Intent(origin, ShowLocationsActivity::class.java)
    intent.putExtra(R.string.username.toString(), username)
    origin.startActivity(intent)
}


