package org.bikerent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bikerent.api.RetrofitClient
import org.bikerent.api.model.Bike
import org.bikerent.api.model.Message
import org.bikerent.databinding.ActivityAdminCreateBikeBinding
import org.nosemaj.kosmos.Tokens
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdminCreateBikeActivity : AppCompatActivity() {
    private lateinit var view: ActivityAdminCreateBikeBinding
    private val auth get() = (applicationContext as BikeRentApp).auth
    private var username = R.string.empty.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityAdminCreateBikeBinding.inflate(layoutInflater)
        setContentView(view.root)

        username = getProperty(R.string.username.toString())

        view.createBikeButton.isEnabled = true
        view.createBikeButton.setOnClickListener {
            view.createBikeButton.isEnabled = false
            createBike()
        }

        view.backButton.isEnabled = true
        view.backButton.setOnClickListener {
            view.backButton.isEnabled = false
            back()
        }
    }

    private fun back() {
        goToAdminLandingPage(this@AdminCreateBikeActivity, username)
    }

    private fun createBike() {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> createBike(token.idToken)
                else -> goToSignIn(source = this@AdminCreateBikeActivity, username)
            }
        }
    }

    private fun createBike(token: String) {

        val manufacturer = view.manufacturer.text.toString()
        if (manufacturer.isEmpty()) {
            displayMessage("Insert manufacturer data")
            return
        }
        val year = view.year.text.toString()
        if (year.isEmpty()) {
            displayMessage("Insert year data")
            return
        }
        val location = view.location.text.toString()
        if (location.isEmpty()) {
            displayMessage("Insert location data")
            return
        }

        val bike = Bike(manufacturer, year, location)
        val call = RetrofitClient.getInstance().service.creteBike(token, bike)

        call.enqueue(object : Callback<Message> {
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                displayMessage(response.body()?.message)
                clearInputs()
            }

            override fun onFailure(call: Call<Message>, t: Throwable) {
                displayMessage("An error has occurred " + t.message)
            }})
    }

    private fun clearInputs() {
        view.createBikeButton.isEnabled = false
        view.location.setText("")
        view.manufacturer.setText("")
        view.year.setText("")
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

fun goToAdminCreateBikePage(origin: Activity, username: String) {
    val intent = Intent(origin, AdminCreateBikeActivity::class.java)
    intent.putExtra(R.string.username.toString(), username)
    origin.startActivity(intent)
}


