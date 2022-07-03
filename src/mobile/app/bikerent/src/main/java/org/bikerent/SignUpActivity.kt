package org.bikerent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.nosemaj.kosmos.Registration.ConfirmedRegistration
import org.nosemaj.kosmos.Registration.UnconfirmedRegistration
import org.bikerent.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var view: ActivitySignUpBinding
    private val auth get() = (applicationContext as BikeRentApp).auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(view.root)
        view.submitButton.setOnClickListener { submitSignIn() }
    }

    private fun submitSignIn() {
        lifecycleScope.launch {
            val email = view.email.text.toString()
            val password = view.password.text.toString()
            try {
                val registration = withContext(Dispatchers.IO) {
                    auth.registerUser(email, password, mapOf("email" to email))
                }
                view.errorMessage.text = "";
                when (registration) {
                    is ConfirmedRegistration -> goToSignIn(this@SignUpActivity, email)
                    is UnconfirmedRegistration -> goToConfirmSignUp(this@SignUpActivity, email)
                }
            } catch (e: Exception) {
                view.errorMessage.text = e.message;
            }
        }
    }
}

fun goToSignUp(source: Activity) {
    val signUpIntent = Intent(source, SignUpActivity::class.java)
    source.startActivity(signUpIntent)
}
