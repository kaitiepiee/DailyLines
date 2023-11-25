package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

/**
 * This class handles the application's splash screen which is shown immediately
 * as the app runs.
 */
class SplashActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val splashDuration = 2000 // 2 seconds
        Handler().postDelayed({
            // Start the main activity after the splash duration
            // if u want to by pass the login, change SignupActivity to MainActivity
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)

            startActivity(intent)
            finish()
        }, splashDuration.toLong())
    }
}