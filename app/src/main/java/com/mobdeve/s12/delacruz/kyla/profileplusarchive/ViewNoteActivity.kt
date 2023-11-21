package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.common.base.Ascii.toLowerCase
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.databinding.ActivityViewNoteBinding
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ViewNoteActivity : AppCompatActivity() {
    companion object {
        const val titleKey : String = "TITLE_KEY"
        const val bodyKey : String = "BODY_KEY"
        const val dateKey : String = "DATE_KEY"
//        const val positionKey: String = "POSITION_KEY"
    }

//    private lateinit var titleString: String
//    private lateinit var bodyString: String
    private lateinit var viewBinding: ActivityViewNoteBinding   // Holds the views of the ActivityViewNoteBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.viewBinding = ActivityViewNoteBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // gets the title, body, date, and day objects from the detail view
        val detailTitle = viewBinding.titleTv
        val detailBody = viewBinding.bodyTv
        val dateCreated = viewBinding.dateCreated
        val dayCreated = viewBinding.dayCreated

        // get the values passed by intent that correspond to title, body, and set title in the detail view
        val titleData = intent.getStringExtra(titleKey)
        detailTitle.text = titleData
        val bodyData = intent.getStringExtra(bodyKey)
        detailBody.text = bodyData

        // Get the values for date and display it
        var fullDateString = intent.getStringExtra(dateKey)
        var parts = fullDateString?.split("-")
        var year = parts?.get(0)
        var day = parts?.get(2)
        var month = parts?.get(1)?.let { Month.of(it.toInt()) }
        var stringMonth = toLowerCase(month.toString())
        stringMonth = stringMonth.replaceFirstChar(Char::titlecase)
        dateCreated.text = "$stringMonth $day, $year"

        // Find the day of the week the entry was created (based on datestring) and display
        val date = LocalDate.parse(fullDateString, DateTimeFormatter.ISO_DATE)
        var dateData = toLowerCase(date.dayOfWeek.toString())
        dateData = dateData.replaceFirstChar(Char::titlecase)
        dayCreated.text = dateData

        //        val position = intent.getIntExtra(positionKey, 0)
        // save as strings
//        titleString = titleData.toString()
//        bodyString = bodyData.toString()
    }
}