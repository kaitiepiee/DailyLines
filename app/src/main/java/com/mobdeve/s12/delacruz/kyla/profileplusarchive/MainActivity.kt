package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.databinding.ActivityArchivesBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Random



class MainActivity : AppCompatActivity(){
    private lateinit var entryList: ArrayList<EntryModel>
    private lateinit var myAdapter: MyAdapter
    private lateinit var viewBinding: ActivityArchivesBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarView: CalendarView
    private lateinit var quoteTextView: TextView
    private lateinit var authorTextView: TextView

    private val viewNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            // The activity was launched and returned a successful result
            val data: Intent? = result.data
            // Process the data or take action based on the result here
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            // The activity was canceled by the user
            // Handle cancellation here, if needed
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content view to the home_screen layout
        setContentView(R.layout.home_screen)

        // For quotes API
        quoteTextView = findViewById(R.id.quote)
        authorTextView = findViewById(R.id.author)
        RequestManager(this@MainActivity).getAllQuotes(listener)

        // For mood buttons
        fun onMoodButtonClick(view: View) {
            val moodTag = view.tag.toString()

            val parentLayout = view.parent as LinearLayout
            for (i in 0 until parentLayout.childCount) {
                val childView = parentLayout.getChildAt(i)
                if (childView is ImageButton && childView != view) {
                    childView.isSelected = false
                    // Restore the original background of the button when not selected
                    childView.setBackgroundResource(android.R.color.transparent)
                }
            }

            view.isSelected = !view.isSelected
            if (view.isSelected) {
                view.setBackgroundResource(R.drawable.glow_background)
                Toast.makeText(this, "Selected mood: $moodTag", Toast.LENGTH_SHORT).show()
            }
            else {
                view.setBackgroundResource(android.R.color.transparent)
                Toast.makeText(this, "Deselected mood: $moodTag", Toast.LENGTH_SHORT).show()

            }
        }

        // TODO: input moods
        // Days of the Week
        val currentDate = LocalDate.now()
        val firstDayOfWeek = currentDate.with(DayOfWeek.MONDAY)
        val dayFormatter = DateTimeFormatter.ofPattern("dd")

        val dayNumbersOfWeek = (0 until 7).map {
            val date = firstDayOfWeek.plusDays(it.toLong())
            date.format(dayFormatter)
        }

        val monDayTextView: TextView = findViewById(R.id.monDay)
        monDayTextView.text = dayNumbersOfWeek[0]
        val tuesDayTextView: TextView = findViewById(R.id.tuesDay)
        tuesDayTextView.text = dayNumbersOfWeek[1]
        val wedDayTextView: TextView = findViewById(R.id.wedDay)
        wedDayTextView.text = dayNumbersOfWeek[2]
        val thuDayTextView: TextView = findViewById(R.id.thuDay)
        thuDayTextView.text = dayNumbersOfWeek[3]
        val friDayTextView: TextView = findViewById(R.id.friDay)
        friDayTextView.text = dayNumbersOfWeek[4]
        val satDayTextView: TextView = findViewById(R.id.satDay)
        satDayTextView.text = dayNumbersOfWeek[5]
        val sunDayTextView: TextView = findViewById(R.id.sunDay)
        sunDayTextView.text = dayNumbersOfWeek[6]

        val worseMoodButton: ImageButton = findViewById(R.id.worseMood)
        worseMoodButton.setOnClickListener { onMoodButtonClick(it) }
        val badMoodButton: ImageButton = findViewById(R.id.badMood)
        badMoodButton.setOnClickListener { onMoodButtonClick(it) }
        val neutralMoodButton: ImageButton = findViewById(R.id.neutralMood)
        neutralMoodButton.setOnClickListener { onMoodButtonClick(it) }
        val goodMoodButton: ImageButton = findViewById(R.id.goodMood)
        goodMoodButton.setOnClickListener { onMoodButtonClick(it) }
        val bestMoodButton: ImageButton = findViewById(R.id.bestMood)
        bestMoodButton.setOnClickListener { onMoodButtonClick(it) }

        // Create an Intent to navigate to the create_journal.xml layout
        val submitEntryButton = findViewById<Button>(R.id.submitEntryButton)
        submitEntryButton.setOnClickListener {
            val intent = Intent(this, NewEntryActivity::class.java)
            startActivity(intent)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Set an OnItemSelectedListener for the BottomNavigationView
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Handle the home screen behavior
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_profile -> {
                    // Navigate to the profile screen when the "Profile" menu item is selected
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_archive -> {
                    // Handle the home screen behavior
                    setupArchives()
                }
            }
            true
        }

    }
    private fun setupArchives() {
        // Inflate the Archives layout
        this.viewBinding = ActivityArchivesBinding.inflate(layoutInflater)
        setContentView(this.viewBinding.root)
        this.entryList = EntryGenerator.generateData()
        recyclerView = viewBinding.recyclerView
        calendarView = viewBinding.calendarView

        // On first open of archives page
        val selectedDate = LocalDate.now().toString() // returns "year-month-day"
        val currentEntries = entryList.filter { it.dateString == selectedDate }
        myAdapter = MyAdapter(currentEntries, viewNoteLauncher)
        recyclerView.adapter = myAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Upon change of date in archive page
        calendarView.setOnDateChangeListener { _, i, i1, i2 ->
            var year = i.toString()
            var month = (i1 + 1).toString()
            var day = i2.toString()
            var newSelectedDate = "$year-$month-$day"
            val currentEntries = entryList.filter { it.dateString == newSelectedDate }
            myAdapter = MyAdapter(currentEntries, viewNoteLauncher)
            recyclerView.adapter = myAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)
        }
        val exitButton = findViewById<ImageView>(R.id.cancelButton)
        exitButton.setOnClickListener {            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // for Quotes API
    private val listener: QuotesResponseListener = object : QuotesResponseListener {
        override fun didFetch(response: List<QuotesResponse>?, message: String, message1: String) {
            runOnUiThread {
                if (!response.isNullOrEmpty()) {
                    // randomize quote from API list
                    val random = Random()
                    val randomQuoteIndex = random.nextInt(response.size)
                    val randomQuote = response[randomQuoteIndex]

                    val quoteText = randomQuote.text
                    val authorText = randomQuote.author

                    // Add """" at the beginning and end of quote
                    val formattedQuote = "\"$quoteText\""
                    // Add "-" at the beginning of the author
                    val formattedAuthor = "- $authorText"

                    // Remove ", type.fit" from the author if it exists
                    val cleanedAuthor = formattedAuthor.replace(", type.fit", "")

                    Log.d("QuoteDebug", "Quote Text: $quoteText, Author: $authorText")
                    quoteTextView.text = formattedQuote
                    authorTextView.text = cleanedAuthor
                }
                else {
                    Log.d("QuoteDebug", "No quotes found")
                    quoteTextView.text = "No quotes found"
                    authorTextView.text = ""
                }
            }
        }

        override fun didError(message: String) {
            Log.e("QuoteError", message)
        }
    }
}

