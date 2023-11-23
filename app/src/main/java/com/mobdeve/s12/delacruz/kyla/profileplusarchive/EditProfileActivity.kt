package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditProfileActivity : AppCompatActivity() {
    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var appPreferences: AppPreferences
    private lateinit var currentUser : UserModel

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    private val COLLECTION_USERS = "Users"
    private val FIELD_USER_ID = "user_id"
    private val PHOTO_URL = "photoUrl"
    private val EMAIL_ADDR = "email"
    private val PROFILE_NAME = "profileName"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Receiving extras sent from MainActivity/EditProfileActivity
        val userID = intent.getStringExtra("userID")

        // App Preferences for Dark/Normal Mode
        appPreferences = AppPreferences(this)

        if (appPreferences.isDarkModeEnabled) {
            setContentView(R.layout.dark_edit_profile)
        } else {
            setContentView(R.layout.edit_profile)
        }

        val nameETV = findViewById<EditText>(R.id.nameETV)
        val profilePivIv = findViewById<ShapeableImageView>(R.id.profilePivIv)
        getUser(userID.toString())

        // For viewing purposes
        nameETV.hint = currentUser.profileName
        profilePivIv.setImageURI(Uri.parse(currentUser.photoUrl))

        // Change profile pic button
        profilePivIv.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }

        // Save button
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            val newName = nameETV.text.toString()

            if (profilePivIv != null && newName.isNotBlank()) {
                // Update user details
                uploadImageToFirebase(userID.toString(), selectedImageUri!!)
            }

            if(newName.isNotBlank()) {
                updateUserName(userID.toString(), newName)
            }

            // Go back Profile Screen
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userID", this.currentUser.user_id)
            startActivity(intent)
        }
    }

    private fun getUser(user_id: String) {
        db.collection(COLLECTION_USERS)
            .whereEqualTo(FIELD_USER_ID, user_id)
            .get()
            .addOnSuccessListener { documents ->
                val document = documents.first()
                val email = document.get(EMAIL_ADDR).toString()
                val profileName = document.get(PROFILE_NAME).toString()
                val photoUrl = document.get(PHOTO_URL).toString()
                val userID = document.get(FIELD_USER_ID).toString()

                this.currentUser = UserModel(email, profileName, photoUrl, userID)

            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error getting documents: $error", Toast.LENGTH_LONG).show()
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!

            // Load the image into the profilePivIv
            val profilePivIv = findViewById<ShapeableImageView>(R.id.profilePivIv)
            profilePivIv.setImageURI(imageUri)
        }
    }

    private fun updateUserName(userID: String, newName: String) {
        val db = FirebaseFirestore.getInstance()

        val query = db.collection(COLLECTION_USERS)
            .whereEqualTo(FIELD_USER_ID, userID)

        // Updating Profile Name
        query.get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val document = documents.first()
                document.reference.update(PROFILE_NAME, newName)
                    .addOnSuccessListener {
                        Log.d(ContentValues.TAG, "Profile name successfully updated!")
                    }
                    .addOnFailureListener { error ->
                        Log.e(ContentValues.TAG, "Error updating document", error)
                    }
            } else {
                Log.e(ContentValues.TAG, "No document found")
            }
        }
        .addOnFailureListener { error ->
            Log.e(ContentValues.TAG, "Error getting documents", error)
        }
    }

    private fun uploadImageToFirebase(userID: String, imageUri: Uri) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef: StorageReference = storageRef.child("images/${System.currentTimeMillis()}_profile_image")
        val db = FirebaseFirestore.getInstance()

        // Uploading url in firebase
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val query = db.collection(COLLECTION_USERS)
                        .whereEqualTo(FIELD_USER_ID, userID)

                    // Updating profile url in the database
                    query.get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.first()
                            document.reference.update(PHOTO_URL, downloadUri.toString())
                                .addOnSuccessListener {
                                    Log.d(ContentValues.TAG, "Photo Url successfully updated!")
                                }
                                .addOnFailureListener { error ->
                                    Log.e(ContentValues.TAG, "Error updating document", error)
                                }
                        } else {
                            Log.e(ContentValues.TAG, "No document found")
                        }
                    }
                    .addOnFailureListener { error ->
                        Log.e(ContentValues.TAG, "Error getting documents", error)
                    }
                }
            }
            .addOnFailureListener { error ->
                Log.e(ContentValues.TAG, "Error uploading photo to firebase", error)
            }
    }
}