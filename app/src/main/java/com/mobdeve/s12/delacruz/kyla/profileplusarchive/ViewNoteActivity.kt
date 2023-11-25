package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.common.base.Ascii.toLowerCase
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.databinding.ActivityViewNoteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

/**
 * This class handles the view journal entry under the Archive screen.
 * By clicking on any date, the user is presented with the journals logged
 * in their account. They may click the entry to review previous journals.
 */
class ViewNoteActivity : AppCompatActivity() {
    companion object {
        const val titleKey : String = "TITLE_KEY"
        const val bodyKey : String = "BODY_KEY"
        const val dateKey : String = "DATE_KEY"
        const val imageKey : String = "IMAGE_KEY"
    }
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
        val imageToDisplay = viewBinding.img
        val imageScrollView = viewBinding.imgScroll

        // makes the body scrollable
        detailBody.movementMethod = ScrollingMovementMethod.getInstance()

        // get the values passed by intent that correspond to title, body, and set title in the detail view
        val titleData = intent.getStringExtra(titleKey)
        detailTitle.text = titleData
        val bodyData = intent.getStringExtra(bodyKey)
        detailBody.text = bodyData
        val imageString = intent.getStringExtra(imageKey)

        // shows the image from the db in the entry by turning the string into the uri, removes the section if theres no image
        if(imageString == ""){
            imageScrollView.visibility = View.GONE
        } else{
            downloadImage(imageString!!, imageToDisplay)
        }

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

        // Closes the current entry and goes back to archives page
        val exitButton = findViewById<ImageView>(R.id.cancelButton)
        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun downloadImage(filename: String, imageToDisplay: ImageView) = CoroutineScope(Dispatchers.IO).launch{
        try {
            val imageRef = Firebase.storage.reference
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = imageRef.child("images/$filename").getBytes(maxDownloadSize).await()
            val bmp = BitmapFactory.decodeByteArray(bytes,0, bytes.size)
            withContext(Dispatchers.Main){
                imageToDisplay.setImageBitmap(bmp)
            }
        }
        catch (e : Exception) {
            withContext(Dispatchers.Main){
                Toast.makeText(this@ViewNoteActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}