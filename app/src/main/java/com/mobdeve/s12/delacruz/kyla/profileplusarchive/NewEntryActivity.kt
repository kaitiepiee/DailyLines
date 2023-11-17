package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NewEntryActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_journal)

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

        // Submit Button
        // TODO: Add submission to database
        val submitEntryButton = findViewById<Button>(R.id.submitButton)
        submitEntryButton.setOnClickListener {
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

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully
                // Get the download URL and store it in the database
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Store the downloadUri in the database along with other journal entry details
                }
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful upload
            }
    }
}