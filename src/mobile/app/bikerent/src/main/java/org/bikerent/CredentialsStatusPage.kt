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
import org.bikerent.databinding.ActivityCredentialsStatusBinding
import org.nosemaj.kosmos.Tokens
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CredentialsStatusPage : AppCompatActivity() {
    private lateinit var view: ActivityCredentialsStatusBinding
    private val auth get() = (applicationContext as BikeRentApp).auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityCredentialsStatusBinding.inflate(layoutInflater)
        setContentView(view.root)
        view.signOutButton.setOnClickListener {
            signOut()
        }
        displayMessage(intent.getStringExtra("message"))
        navigate()
    }

    private fun signOut() {
        lifecycleScope.launch {
            auth.signOut()
            view.sessionInfo.visibility = INVISIBLE
            displayMessage("Signed out!")
        }
    }

    private fun navigate() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> displayLocations(token.idToken)
                else -> goToSignIn(source = this@CredentialsStatusPage)
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
                    val selectedItem = parent.getItemAtPosition(position) as String
                    displayMessage( "Selected $selectedItem")
                }
                view.message.visibility = INVISIBLE


            }

            override fun onFailure(call: Call<List<String?>?>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
                view.locationList.visibility = INVISIBLE
            }
        })

    }
}

fun goToCredentialsStatus(origin: Activity, message: String) {
    val intent = Intent(origin, CredentialsStatusPage::class.java)
    intent.putExtra("message", message)
    origin.startActivity(intent)
}
