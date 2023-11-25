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
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * This class allows the user to update their account's profile.
 * They may edit their profile picture & profile name.
 * The data is stored and updated in the project's Firestore dabatase.
 *
 */
class EditProfileActivity : AppCompatActivity() {
    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var appPreferences: AppPreferences
    private lateinit var mAuth: FirebaseAuth

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

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
            setContentView(R.layout.dark_edit_profile)
        } else {
            setContentView(R.layout.edit_profile)
        }

        // Getting elements
        val nameETV = findViewById<EditText>(R.id.nameETV)
        val profilePivIv = findViewById<ShapeableImageView>(R.id.profilePivIv)
        val saveButton = findViewById<Button>(R.id.saveButton)

        mAuth = FirebaseAuth.getInstance()
        // Fetch the current user from Firebase Authentication
        val currentUser: FirebaseUser? = mAuth.currentUser

        if (currentUser != null) {
            getUser(currentUser.email.toString()) { item ->
                if(item != null) {
                    // Update UI
                    nameETV.hint = item.profileName
                    Glide.with(this).load(item.photoUrl).into(profilePivIv)

                    // Change profile pic button
                    profilePivIv.setOnClickListener {
                        val galleryIntent = Intent(Intent.ACTION_PICK)
                        galleryIntent.type = "image/*"
                        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
                    }

                    // Save changes
                    saveButton.setOnClickListener {
                        val newName = nameETV.text.toString()
                        var nameIsDone = false
                        var picIsDone = false

                        if(newName.isNotBlank()) {
                            updateUserName(item.user_id, newName) {
                                nameIsDone = true
                            }
                        } else {
                            nameIsDone = true
                        }

                        if(selectedImageUri != null) {
                            uploadImageToFirebase(item.user_id, selectedImageUri!!) {
                                picIsDone = true
                            }
                        } else {
                            picIsDone = true
                        }

                        if(nameIsDone && picIsDone){
                            val intent = Intent(this, ProfileActivity::class.java)
                            startActivity(intent)
                        }

                    }
                }
            }
        }
    }

    // Gets user information from the DB
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
                Log.e("GetUserDB", "Error getting document", error)
                onUserLoaded(null)
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            this.selectedImageUri = data.data!!

            // Load the image into the profilePivIv
            val profilePivIv = findViewById<ShapeableImageView>(R.id.profilePivIv)
            profilePivIv.setImageURI(selectedImageUri)
        }
    }

    // Updates the user's Profile Name in the DB
    private fun updateUserName(userID: String, newName: String, callback: () -> Unit) {
        Log.d(ContentValues.TAG, "AM I IN HERE $newName")
        val db = FirebaseFirestore.getInstance()

        val query = db.collection(COLLECTION_USERS)
            .whereEqualTo(FIELD_USER_ID, userID)

        // Updating Profile Name
        query.get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                Log.d(ContentValues.TAG, "HOW ABOUT HERE $newName")
                val document = documents.first()
                document.reference.update(PROFILE_NAME, newName)
                    .addOnSuccessListener {
                        profileName = newName
                        Log.d("UpdateName", "Profile name successfully updated!")
                        callback.invoke()
                    }
                    .addOnFailureListener { error ->
                        Log.e("UpdateName", "Error updating document", error)
                    }
            } else {
                Log.e("UpdateName", "No document found")
            }
        }
        .addOnFailureListener { error ->
            Log.e("UpdateName", "Error getting documents", error)
        }
    }

    // Uploads selected image to firebase then adds its URL to the DB
    private fun uploadImageToFirebase(userID: String, imageUri: Uri, callback: () -> Unit) {
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
                                    Log.d("UploadImage", "Photo Url successfully updated!")
                                    callback.invoke()
                                }
                                .addOnFailureListener { error ->
                                    Log.e("UploadImage", "Error updating document", error)
                                }
                        } else {
                            Log.e("UploadImage", "No document found")
                        }
                    }
                    .addOnFailureListener { error ->
                        Log.e("UploadImage", "Error getting documents", error)
                    }
                }
            }
            .addOnFailureListener { error ->
                Log.e("UploadImage", "Error uploading photo to firebase", error)
            }
    }
}