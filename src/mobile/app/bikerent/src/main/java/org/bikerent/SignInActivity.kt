package org.bikerent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bikerent.databinding.ActivitySignInBinding
import org.nosemaj.kosmos.Auth
import org.nosemaj.kosmos.Tokens

class SignInActivity : AppCompatActivity() {
    private lateinit var view: ActivitySignInBinding
    private val auth get() = (applicationContext as BikeRentApp).auth
    private val COGNITO_GROUPS = "cognito:groups"
    private val COGNITO_ADMIN_GROUP = "admin-users-group"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(view.root)
        view.username.setText(intent.getStringExtra("username"))
        view.submitButton.isEnabled = true
        view.submitButton.setOnClickListener {
            view.submitButton.isEnabled = false
            signIn()
            view.submitButton.isEnabled = true
        }
        view.signUpLinkText.isEnabled = true
        view.signUpLinkText.setOnClickListener {
            view.submitButton.isEnabled = false
            goToSignUp(source = this)
            view.submitButton.isEnabled = true
        }
    }

    private fun signIn() {
        lifecycleScope.launch {
            val username = view.username.text.toString()
            val password = view.password.text.toString()

            if (!validUsername(username) || !validPassword(password)) {
                return@launch
            }

            try {
                auth.signIn(username, password)
            } catch (e: Exception) {
                displayMessage(Gson().fromJson(e.message, SignUpActivity.AWSErrorResponse::class.java).message);
                return@launch
            }
            displayMessage(null)

            when (val token = auth.tokens()) {
                is Tokens.ValidTokens -> {
                    if (isUserInAdminGroup(token.idToken)) {
                        goToAdminLandingPage(this@SignInActivity, username)
                    } else {
                        goToShowLocationsPage(this@SignInActivity, username)
                    }
                }
                else -> displayMessage("Try restarting application!")
            }
        }
    }

    private fun validPassword(password: String): Boolean {
        if (password.isEmpty()) {
            displayMessage("Please enter password")
            return false
        }
        return true
    }

    private fun validUsername(username: String): Boolean {
        if (username.isEmpty()) {
            displayMessage("Please enter password")
            return false
        }

        if (!username.contains(Regex("@"))) {
            displayMessage("Email does not contain @ character")
            return false
        }
        return true
    }

    private fun isUserInAdminGroup(token: String): Boolean {
        val jwt = JWT(token)
        return try {
            jwt.getClaim(COGNITO_GROUPS).asArray(String::class.java)[0].equals(COGNITO_ADMIN_GROUP)
        } catch (e: Exception) {
            false
        }
    }

    private fun displayMessage(text: String?) {
        if (text != null) {
            view.message.text = text
            view.message.visibility = View.VISIBLE
        } else {
            view.message.visibility = View.INVISIBLE
        }
    }
}

fun goToSignIn(source: Activity, username: String? = null, isAdmin: Boolean = false) {
    source.finish()
    val intent = Intent(source, SignInActivity::class.java)
    intent.putExtra(R.string.username.toString(), username)
    intent.putExtra(R.string.admin.toString(), isAdmin)
    source.startActivity(intent)
}
