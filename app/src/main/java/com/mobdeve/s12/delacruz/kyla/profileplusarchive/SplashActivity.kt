package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
class SplashActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val splashDuration = 2000 // 2 seconds
        Handler().postDelayed({
            // Start the main activity after the splash duration

            // if u want to by pass the login, change LoginActivity to MainActivity
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, splashDuration.toLong())
    }
}