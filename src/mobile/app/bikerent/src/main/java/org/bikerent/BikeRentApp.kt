package org.bikerent

import android.app.Application
import org.nosemaj.kosmos.Auth

class BikeRentApp : Application() {
    lateinit var auth: Auth

    override fun onCreate() {
        super.onCreate()
        auth = Auth(
            context = this,
            userPoolId = "eu-central-1_uupWnRsp1",
            identityPoolId = "eu-central-1:3e139fd0-28e4-4821-954c-8d6e284a8dee",
            clientId = "7iqli3vnkchjou1rl04rgmi6u5",
            clientSecret = ""
        )
    }
}
