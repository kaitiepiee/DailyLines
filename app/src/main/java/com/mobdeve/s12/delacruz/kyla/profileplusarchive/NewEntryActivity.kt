package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NewEntryActivity : AppCompatActivity() {
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

//        val editProfileButton = findViewById<Button>(R.id.editProfileButton)
//
//        editProfileButton.setOnClickListener {
//            val intent = Intent(this, EditProfileActivity::class.java)
//            startActivity(intent)
//        }

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

}