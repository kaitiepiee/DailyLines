package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class ProfileActivity : AppCompatActivity() {

    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var appPreferences: AppPreferences
    private lateinit var mAuth: FirebaseAuth

    private val COLLECTION_USERS = "Users"
    private val FIELD_USER_ID = "user_id"
    private val PHOTO_URL = "photoUrl"
    private val EMAIL_ADDR = "email"
    private val PROFILE_NAME = "profileName"

    var email = ""
    var profileName = ""
    var photoUrl = ""
    var user_id = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // App Preferences for Dark/Normal Mode
        appPreferences = AppPreferences(this)

        if (appPreferences.isDarkModeEnabled) {
            setContentView(R.layout.dark_profile_screen)
        } else {
            setContentView(R.layout.activity_profile_screen)
        }

        Log.d("ProfileActivity", "Im back here?")

        mAuth = FirebaseAuth.getInstance()
        // Fetch the current user from Firebase Authentication
        val currentUser: FirebaseUser? = mAuth.currentUser

        // Update UI with the user's information
        if (currentUser != null) {
            getUser(currentUser.email.toString()) { item ->
                if(item != null) {
                    this.email = item.email
                    this.profileName = item.profileName
                    this.photoUrl = item.photoUrl
                    this.user_id = item.user_id

                    // Update UI
                    updateUI(item.email, item.profileName, item.photoUrl)
                }
            }
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

    // Update UI after edit profile
    override fun onResume() {
        super.onResume()

        getUser(this.email) { item ->
            if(item != null) {
                this.email = item.email
                this.profileName = item.profileName
                this.photoUrl = item.photoUrl
                this.user_id = item.user_id

                updateUI(item.email, item.profileName, item.photoUrl)
            }
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

    // Display user information in the UI
    private fun updateUI(email : String, profileName : String, photoUrl : String) {
        // Update profile picture
        val profilePictureImageView: ShapeableImageView = findViewById(R.id.profilePivIv)
        if (photoUrl != null) {
            // Load the profile picture using a library like Picasso or Glide
            // For simplicity, assuming you have a library like Glide:
            Glide.with(this).load(photoUrl).into(profilePictureImageView)
        }

        // Update full name
        val fullNameTextView: TextView = findViewById(R.id.fullnameTv)
        if (profileName.isNotBlank()) {
            fullNameTextView.text = profileName
        }

        // Update email address
        val emailTextView: TextView = findViewById(R.id.emailTv)
        if (email.isNotBlank()) {
            emailTextView.text = email
        }
    }

    private fun getUser(email: String, onUserLoaded: (UserModel?) -> Unit) {
        db.collection(COLLECTION_USERS)
            .whereEqualTo(EMAIL_ADDR, email)
            .get()
            .addOnSuccessListener { documents ->
                val userModel: UserModel? = if (documents.isEmpty) null else {
                    val document = documents.first()
                    val emailAddr = document.get(EMAIL_ADDR).toString()
                    val profileName = document.get(PROFILE_NAME).toString()
                    val photoUrl = document.get(PHOTO_URL).toString()
                    val userID = document.get(FIELD_USER_ID).toString()
                    UserModel(emailAddr, profileName, photoUrl, userID)
                }
                onUserLoaded(userModel)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_LONG).show()
                onUserLoaded(null)
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
