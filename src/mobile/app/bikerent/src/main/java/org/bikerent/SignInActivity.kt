package org.bikerent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bikerent.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var view: ActivitySignInBinding
    private val auth get() = (applicationContext as BikeRentApp).auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(view.root)
        view.username.setText(intent.getStringExtra("username"))
        view.submitButton.setOnClickListener {
            signIn()
        }
        view.signUpLinkText.setOnClickListener {
            goToSignUp(source = this)
        }
    }

    private fun signIn() {
        lifecycleScope.launch {
            val username = view.username.text.toString()
            val password = view.password.text.toString()
            auth.signIn(username, password)
            goToShowLocationsPage(this@SignInActivity, username)
        }
    }
}

fun goToSignIn(source: Activity, username: String? = null) {
    val intent = Intent(source, SignInActivity::class.java)
    intent.putExtra("username", username)
    source.startActivity(intent)
}
