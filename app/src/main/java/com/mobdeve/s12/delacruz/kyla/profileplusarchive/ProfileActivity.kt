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
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ProfileActivity : AppCompatActivity() {
    private lateinit var appPreferences: AppPreferences
    private lateinit var mAuth: FirebaseAuth

    // Declares the db
    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    // Creates constants for us to call so we don't have to type everything
    private val COLLECTION_EMOTIONS = "Emotions"
    private val COLLECTION_ENTRIES = "Entries"
    private val FIELD_USER_ID = "user_id"
    private val FIELD_DATE = "datestring"

    private val FIELD_EMO_TRACKED = "emotion_tracked"
    private val FIELD_ENT_TITLE = "title"
    private val FIELD_ENT_BODY = "body"
    private val FIELD_ENT_IMG = "image"

    private var current_user_id = ""

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


        if (currentUser != null) {
            // User is signed in, update the welcome message
            current_user_id = currentUser.uid
        }

        // Fetch the GoogleSignInAccount
        val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

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



        // Get a list of all entries for that user in the database: count them and calculate user streak
        db.collection(COLLECTION_ENTRIES)
            .whereEqualTo(FIELD_USER_ID, current_user_id)
            .get()
            .addOnSuccessListener { documents ->
                // counts number of entries and displays number
                var numberOfEntires = documents.size()
                val journalEntriesTextView = findViewById<TextView>(R.id.numJournalEntriesTv)
                journalEntriesTextView.text = "$numberOfEntires"

                // counts number of days in streak and displays number
                var numStreak = 0
                if(!documents.isEmpty){
                    // get list of the dates of all entries made by the user
                    var listOfDates = arrayOf<String>()
                    for(document in documents){
                        listOfDates += document.get(FIELD_DATE).toString()
                    }

                    // sort the dates such that the first is the most recent date
                    listOfDates.sortDescending()

                    // get current date
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val current = LocalDateTime.now().format(formatter)

                    // check if they made an entry today (if none, no streak; if yes, calculate the streak)
                    if(listOfDates[0] == current){
                        var latestDate = LocalDate.parse(current)
                        for((counter, date) in listOfDates.withIndex()){
                            var parsedDate = LocalDate.parse(date)
                            var dateMinus1 = latestDate.minusDays(1)
                            // if the first date is the current date, increment counter once
                            if(counter == 0 && latestDate == parsedDate){ numStreak++ }
                            // for all succeeding, only increment counter if the parsed date is exactly 1 day after the previous date
                            if(parsedDate == dateMinus1){
                                numStreak++
                            }
                            latestDate = parsedDate
                        }
                    }
                }

                val streakTextView = findViewById<TextView>(R.id.longestStreakTv)
                streakTextView.text = "$numStreak"
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: $exception")
            }

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
