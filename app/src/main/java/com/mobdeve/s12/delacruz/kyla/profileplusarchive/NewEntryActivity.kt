package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.R
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.databinding.ActivityArchivesBinding
import java.time.LocalDate

class NewEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_journal)


//        val editProfileButton = findViewById<Button>(R.id.editProfileButton)
//
//        editProfileButton.setOnClickListener {
//            val intent = Intent(this, EditProfileActivity::class.java)
//            startActivity(intent)
//        }

        val exitButton = findViewById<ImageView>(R.id.cancelButton)
        exitButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

}