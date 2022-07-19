package org.bikerent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
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

            if (isValidEmail(email) && isValidPassword(password)){
                displayErrorMessage(null)
                try {
                    val registration = withContext(Dispatchers.IO) {
                        auth.registerUser(email, password, mapOf("email" to email))
                    }
                    when (registration) {
                        is ConfirmedRegistration -> goToSignIn(this@SignUpActivity, email)
                        is UnconfirmedRegistration -> goToConfirmSignUp(this@SignUpActivity, email)
                    }
                } catch (e: Exception) {
                    displayErrorMessage(Gson().fromJson(e.message, AWSErrorResponse::class.java).message);
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isEmpty()) {
            displayErrorMessage("Username required!")
            return false
        }

        if (!email.contains(Regex("@"))) {
            displayErrorMessage("Email does not contain @ character")
            return false
        }
        return true
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.isEmpty()) {
            displayErrorMessage("Password required!")
            return false
        }

        if (password.length < 8) {
            displayErrorMessage("Password too shot, 8 required!")
            return false
        }

        if (!password.contains(Regex("[0-9]"))) {
            displayErrorMessage("Password does not contain number!")
            return false
        }

        if (!password.contains(Regex("[a-z]"))) {
            displayErrorMessage("Password does not contain lowercase number!")
            return false
        }

        if (!password.contains(Regex("[A-Z]"))) {
            displayErrorMessage("Password does not contain uppercase number!")
            return false
        }

        if (!password.contains(Regex("[@#\$%^&+=!]"))) {
            displayErrorMessage("Password does not contain symbol!")
            return false
        }

        return true
    }


    private fun displayErrorMessage(text: String?) {
        if (text != null) {
            view.errorMessage.text = text
            view.errorMessage.visibility = View.VISIBLE
        } else {
            view.errorMessage.visibility = View.INVISIBLE
        }
    }


    data class AWSErrorResponse(
        val message: String
    )


}



fun goToSignUp(source: Activity) {
    val signUpIntent = Intent(source, SignUpActivity::class.java)
    source.startActivity(signUpIntent)
}
