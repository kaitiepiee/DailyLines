package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    var isDarkMode = false;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // light mode
        if (!isDarkMode) {
            setContentView(R.layout.activity_profile_screen)
        }
        // dark mode
        else {
            setContentView(R.layout.dark_profile_screen)
        }

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

        // Settings Button
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            showSettings(it)
        }

    }

    private fun showSettings(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.settings_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings_mode -> {
                    if (isDarkMode) {
                        isDarkMode = !isDarkMode
                    } else {
                        isDarkMode = !isDarkMode
                    }
                    true
                }

                R.id.settings_edit -> {
//                    val intent = Intent(this, EditProfileActivity::class.java)
//                    startActivity(intent)
                    true
                }

                R.id.settings_logout -> {
                    signOut()
                    true
                }

                else -> false
            }
        }

        popup.show()
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
