package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


abstract class ProfileActivity(
    private var bestMood: TextView?,
    private var goodMood: TextView?,
    private var neutralMood: TextView?,
    private var badMood: TextView?,
    private var worstMood: TextView?,
    private var pieChart: PieChart?
) : AppCompatActivity() {
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

        // Initialize views
        pieChart = findViewById(R.id.piechart)
        bestMood = findViewById(R.id.bestMood)
        goodMood = findViewById(R.id.goodMood)
        neutralMood = findViewById(R.id.neutralMood)
        badMood = findViewById(R.id.badMood)
        worstMood = findViewById(R.id.worstMood)

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
        updateUI(currentUser, googleSignInAccount)

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

        // Creating a method setData()
        // to set the text in text view and pie chart
        setData();
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

    @SuppressLint("SetTextI18n")
    private fun setData() {

        // Set the percentage of language used
        bestMood?.text = 40.toString()
        goodMood?.text = 30.toString()
        neutralMood?.text = 5.toString()
        badMood?.text = 10.toString()
        worstMood?.text = 15.toString()


        // Set the data and color to the pie chart
        pieChart?.addPieSlice(
            PieModel(
                "best mood", bestMood?.text.toString().toInt().toFloat(),
                Color.parseColor("#FFA726")
            )
        )
        pieChart?.addPieSlice(
            PieModel(
                "good mood", goodMood?.text.toString().toInt().toFloat(),
                Color.parseColor("#66BB6A")
            )
        )
        pieChart?.addPieSlice(
            PieModel(
                "neutral mood", neutralMood?.text.toString().toInt().toFloat(),
                Color.parseColor("#EF5350")
            )
        )
        pieChart?.addPieSlice(
            PieModel(
                "bad mood", badMood?.text.toString().toInt().toFloat(),
                Color.parseColor("#29B6F6")
            )
        )
        pieChart?.addPieSlice(
            PieModel(
                "worst mood", worstMood?.text.toString().toInt().toFloat(),
                Color.parseColor("#29B6F6")
            )
        )

        // To animate the pie chart
        pieChart?.startAnimation()
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
