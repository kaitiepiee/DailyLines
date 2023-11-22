package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NewEntryActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var auth: FirebaseAuth

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

    private val addToDB = HashMap<String,Any>()
    private var entryImage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.applyDarkModeLogic(this, R.layout.create_journal, R.layout.dark_create_journal)

        // After initializing views and Firebase Authentication
        auth = FirebaseAuth.getInstance()
        // Check if the user is signed in
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            // User is signed in, update the welcome message
            current_user_id = currentUser.uid
        }

        val dateTextView = findViewById<TextView>(R.id.dateCreated)
        val dayOfWeekTextView = findViewById<TextView>(R.id.dayCreated)

        // Get the current date
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val currentDate: String = dateFormat.format(Date())

        // Get the current day of the week
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val currentDayOfWeek = dayOfWeekFormat.format(Date())

        // Set the current date in the TextView
        dateTextView.text = currentDate

        // Set the current day of the week in the TextView
        dayOfWeekTextView.text = currentDayOfWeek;

        // Add Image Button
        val addImgButton = findViewById<ImageButton>(R.id.addImgButton)
        addImgButton.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }

        // access the title and body text views
        val titleTextView = findViewById<TextView>(R.id.titleTv)
        val bodyTextView = findViewById<TextView>(R.id.bodyTv)

        // Submit Button -- adds submission to our database
        val submitEntryButton = findViewById<Button>(R.id.submitButton)
        submitEntryButton.setOnClickListener {

            // Get the values for title and body
            var titleString = titleTextView.text.toString()
            var bodyString = bodyTextView.text.toString()

            // Makes the body scrollable
            bodyTextView.movementMethod = ScrollingMovementMethod.getInstance()

            // Get the current date and format it into appropriate datestring
            val preDateString = SimpleDateFormat("yyyy-M-dd", Locale.getDefault())
            var dateString = preDateString.format(Date())

//            Toast.makeText(this, "${this.entryImage}", Toast.LENGTH_SHORT).show()

            // Pass these to the database
            this.addToDB[FIELD_ENT_TITLE] = titleString
            this.addToDB[FIELD_ENT_BODY] = bodyString
            this.addToDB[FIELD_DATE] = dateString
            this.addToDB[FIELD_USER_ID] = this.current_user_id
            this.addToDB[FIELD_ENT_IMG] = this.entryImage
            db.collection(COLLECTION_ENTRIES)
                .add(addToDB)
                .addOnSuccessListener {
                    Log.d(TAG, "Data added")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Data not added")
                }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Exit Button
        val exitButton = findViewById<ImageView>(com.mobdeve.s12.delacruz.kyla.profileplusarchive.R.id.cancelButton)
        exitButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imgPreview = findViewById<ImageView>(R.id.imgPreview)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!

            // Upload the image to Firebase Storage
            uploadImageToFirebase(imageUri)
            // Load the image into the preview ImageView
            imgPreview.setImageURI(imageUri)
            // Make the preview ImageView visible
            imgPreview.visibility = View.VISIBLE
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef: StorageReference = storageRef.child("images/${System.currentTimeMillis()}_journal_image")

//        Toast.makeText(this, "A ${imageRef.toString()}", Toast.LENGTH_SHORT).show()
        this.entryImage = "images/${System.currentTimeMillis()}_journal_image" // TO DO : What am I supposed to pass to the DB?

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully
                // Get the download URL and store it in the database
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Store the downloadUri in the database along with other journal entry details
                    // Give the image uri to entry image so we can pass it to the db
//                    this.entryImage = downloadUri.toString() // TO DO : What am I supposed to pass to the DB?
                }
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful upload
            }
    }
}