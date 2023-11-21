package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class ProfileActivity : AppCompatActivity() {

    private lateinit var appPreferences: AppPreferences
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// App Preferences for Dark/Normal Mode
        appPreferences = AppPreferences(this)

        if (appPreferences.isDarkModeEnabled) {
            setContentView(R.layout.dark_profile_screen)
        } else {
            setContentView(R.layout.activity_profile_screen)
        }

        mAuth = FirebaseAuth.getInstance()
        // Fetch the current user from Firebase Authentication
        val currentUser: FirebaseUser? = mAuth.currentUser

        // Fetch the GoogleSignInAccount
        val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

        // Update UI with the user's information
        updateUI(currentUser, googleSignInAccount)

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
                    appPreferences.isDarkModeEnabled = !appPreferences.isDarkModeEnabled
                    recreate() // Recreate the activity to apply the new theme
                    true
                }

                R.id.settings_edit -> {
                    val intent = Intent(this, EditProfileActivity::class.java)
                    startActivity(intent)
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

    private fun updateUI(currentUser: FirebaseUser?, googleSignInAccount: GoogleSignInAccount?) {
        // Display user information in the UI
        currentUser?.let {
            // Update profile picture
            val profilePictureImageView: ShapeableImageView = findViewById(R.id.profilePivIv)
            val photoUrl = it.photoUrl
            if (photoUrl != null) {
                // Load the profile picture using a library like Picasso or Glide
                // For simplicity, assuming you have a library like Glide:
                Glide.with(this).load(photoUrl).into(profilePictureImageView)
            }

            // Update full name
            val fullNameTextView: TextView = findViewById(R.id.fullnameTv)
            val displayName = it.displayName
            if (!displayName.isNullOrBlank()) {
                fullNameTextView.text = displayName
            }

            // Update email address
            val emailTextView: TextView = findViewById(R.id.emailTv)
            val email = it.email
            if (!email.isNullOrBlank()) {
                emailTextView.text = email
            }
        }

        // If Firebase user is null, try to fetch information from GoogleSignInAccount
        if (currentUser == null && googleSignInAccount != null) {
            // Update profile picture
            val profilePictureImageView: ShapeableImageView = findViewById(R.id.profilePivIv)
            val photoUrl = googleSignInAccount.photoUrl
            if (photoUrl != null) {
                // Load the profile picture using a library like Picasso or Glide
                // For simplicity, assuming you have a library like Glide:
                Glide.with(this).load(photoUrl).into(profilePictureImageView)
            }

            // Update full name
            val fullNameTextView: TextView = findViewById(R.id.fullnameTv)
            val displayName = googleSignInAccount.displayName
            if (!displayName.isNullOrBlank()) {
                fullNameTextView.text = displayName
            }

            // Update email address
            val emailTextView: TextView = findViewById(R.id.emailTv)
            val email = googleSignInAccount.email
            if (!email.isNullOrBlank()) {
                emailTextView.text = email
            }
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val mGoogleSignInClient = GoogleSignInManager.getGoogleSignInClient(this)
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            Log.d("SignOut", "User signed out")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
