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
import org.bikerent.databinding.ActivityCredentialsStatusBinding
import org.nosemaj.kosmos.Tokens

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
                is Tokens.ValidTokens -> displayBikes(token.idToken)
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

    private fun displayBikes(token: String) {




        view.sessionInfo.visibility = VISIBLE
    }
}

fun goToCredentialsStatus(origin: Activity, message: String) {
    val intent = Intent(origin, CredentialsStatusPage::class.java)
    intent.putExtra("message", message)
    origin.startActivity(intent)
}
