package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_screen)

//        val editProfileButton = findViewById<Button>(R.id.editProfileButton)
//
//        editProfileButton.setOnClickListener {
//            val intent = Intent(this, EditProfileActivity::class.java)
//            startActivity(intent)
//        }

        // Show total number of entries
        val entryListSize = intent.getIntExtra("entryListSize", 0)
        val journalEntriesTextView = findViewById<TextView>(R.id.numJournalEntriesTv)
        journalEntriesTextView.text = "$entryListSize"

        // Exit button
        val exitButton = findViewById<ImageView>(R.id.cancelButton)
        exitButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
