package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditProfileActivity : AppCompatActivity() {
    private lateinit var appPreferences: AppPreferences
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // App Preferences for Dark/Normal Mode
        appPreferences = AppPreferences(this)

        if (appPreferences.isDarkModeEnabled) {
            setContentView(R.layout.dark_edit_profile)
        } else {
            setContentView(R.layout.edit_profile)
        }

        val nameETV = findViewById<EditText>(R.id.nameETV)
        val profilePivIv = findViewById<ShapeableImageView>(R.id.profilePivIv)

        profilePivIv.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }

        // Save button
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            val newName = nameETV.text.toString() // TODO: update name in database

            if (profilePivIv != null) {
                // Upload the image to Firebase Storage
                uploadImageToFirebase(selectedImageUri!!)
            }

            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
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
    private fun uploadImageToFirebase(imageUri: Uri) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef: StorageReference = storageRef.child("images/${System.currentTimeMillis()}_journal_image")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully
                // Get the download URL and store it in the database
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // TODO: update profile pic in database
                }
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful upload
            }
    }
}