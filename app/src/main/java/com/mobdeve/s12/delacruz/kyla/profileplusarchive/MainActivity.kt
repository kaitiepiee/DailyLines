package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.databinding.ActivityArchivesBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Random
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity(){
    private val entryList = ArrayList<EntryModel>()
    private lateinit var emotionList: ArrayList<EmotionModel>
    private lateinit var myAdapter: MyAdapter
    private lateinit var viewBinding: ActivityArchivesBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarView: CalendarView
    private lateinit var quoteTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var appTitleTextView: TextView
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

    private val current_user = "me"

    private val viewNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            // The activity was launched and returned a successful result
            val data: Intent? = result.data
            // Process the data or take action based on the result here
        } else if (result.resultCode == RESULT_CANCELED) {
            // The activity was canceled by the user
            // Handle cancellation here, if needed
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.applyDarkModeLogic(this, R.layout.home_screen, R.layout.dark_home_screen)

        // After initializing views and Firebase Authentication
        appTitleTextView = findViewById(R.id.appTitle)
        auth = FirebaseAuth.getInstance()

// Check if the user is signed in
        val currentUser: FirebaseUser? = auth.currentUser

        if (currentUser != null) {
            // User is signed in, update the welcome message
            val displayName = currentUser.displayName
            val welcomeMessage = "Welcome back, $displayName!"
            appTitleTextView.text = welcomeMessage
        } else {
            // User is not signed in, show a default message or handle accordingly
            appTitleTextView.text = "Daily Lines"
        }
        // For quotes API
        quoteTextView = findViewById(R.id.quote)
        authorTextView = findViewById(R.id.author)
        RequestManager(this@MainActivity).getAllQuotes(listener)

        // Mini Calendar with Moods
        val currentDate = LocalDate.now()
        val firstDayOfWeek = currentDate.with(DayOfWeek.MONDAY)
        val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val datesOfWeek = (0 until 7).map {
            val date = firstDayOfWeek.plusDays(it.toLong())
            date.format(dayFormatter)
        }

        // Monday
        getEmotionOfCurrentUserAndDate(current_user, datesOfWeek[0]) { moodMon ->
            val monIv: ImageView = findViewById(R.id.monMood)
            val monTv: TextView = findViewById(R.id.monDay)

            if (moodMon != null) {
                monTv.visibility = View.GONE
                monIv.visibility = View.VISIBLE
                monIv.setImageResource(getMoodSrc(moodMon.emotion_tracked))
            }
            else {
                monTv.visibility = View.VISIBLE
                monIv.visibility = View.GONE
                monTv.text = datesOfWeek[0].substring(8, 10) // dd
            }
        }

        // Tues
        getEmotionOfCurrentUserAndDate(current_user, datesOfWeek[1]) { moodTues ->
            val tuesIv: ImageView = findViewById(R.id.tuesMood)
            val tuesTv: TextView = findViewById(R.id.tuesDay)

            if (moodTues != null) {
                tuesTv.visibility = View.GONE
                tuesIv.visibility = View.VISIBLE
                tuesIv.setImageResource(getMoodSrc(moodTues.emotion_tracked))
            }
            else {
                tuesTv.visibility = View.VISIBLE
                tuesIv.visibility = View.GONE
                tuesTv.text = datesOfWeek[1].substring(8, 10)
            }
        }

        // Wednesday
        getEmotionOfCurrentUserAndDate(current_user, datesOfWeek[2]) { moodWed ->
            val wedIv: ImageView = findViewById(R.id.wedMood)
            val wedTv: TextView = findViewById(R.id.wedDay)

            if (moodWed != null) {
                wedTv.visibility = View.GONE
                wedIv.visibility = View.VISIBLE
                wedIv.setImageResource(getMoodSrc(moodWed.emotion_tracked))
            }
            else {
                wedTv.visibility = View.VISIBLE
                wedIv.visibility = View.GONE
                wedTv.text = datesOfWeek[2].substring(8, 10)
            }
        }

        // Thursday
        getEmotionOfCurrentUserAndDate(current_user, datesOfWeek[3]) { moodThu ->
            val thuIv: ImageView = findViewById(R.id.thuMood)
            val thuTv: TextView = findViewById(R.id.thuDay)

            if (moodThu != null) {
                thuTv.visibility = View.GONE
                thuIv.visibility = View.VISIBLE
                thuIv.setImageResource(getMoodSrc(moodThu.emotion_tracked))
            }
            else {
                thuTv.visibility = View.VISIBLE
                thuIv.visibility = View.GONE
                thuTv.text = datesOfWeek[3].substring(8, 10)
            }
        }

        // Friday
        getEmotionOfCurrentUserAndDate(current_user, datesOfWeek[4]) { moodFri ->
            val friIv: ImageView = findViewById(R.id.friMood)
            val friTv: TextView = findViewById(R.id.friDay)

            if (moodFri != null) {
                friTv.visibility = View.GONE
                friIv.visibility = View.VISIBLE
                friIv.setImageResource(getMoodSrc(moodFri.emotion_tracked))
            }
            else {
                friTv.visibility = View.VISIBLE
                friIv.visibility = View.GONE
                friTv.text = datesOfWeek[4].substring(8, 10)
            }
        }

        // Saturday
        getEmotionOfCurrentUserAndDate(current_user, datesOfWeek[5]) { moodSat ->
            val satIv: ImageView = findViewById(R.id.satMood)
            val satTv: TextView = findViewById(R.id.satDay)

            if (moodSat != null) {
                satTv.visibility = View.GONE
                satIv.visibility = View.VISIBLE
                satIv.setImageResource(getMoodSrc(moodSat.emotion_tracked))
            }
            else {
                satTv.visibility = View.VISIBLE
                satIv.visibility = View.GONE
                satTv.text = datesOfWeek[5].substring(8, 10)
            }
        }

        // Sunday
        getEmotionOfCurrentUserAndDate(current_user, datesOfWeek[6]) { moodSun ->
            val sunIv: ImageView = findViewById(R.id.sunMood)
            val sunTv: TextView = findViewById(R.id.sunDay)

            if (moodSun != null) {
                sunTv.visibility = View.GONE
                sunIv.visibility = View.VISIBLE
                sunIv.setImageResource(getMoodSrc(moodSun.emotion_tracked))
            }
            else {
                sunTv.visibility = View.VISIBLE
                sunIv.visibility = View.GONE
                sunTv.text = datesOfWeek[6].substring(8, 10)
            }
        }


        // For mood buttons
        fun onMoodButtonClick(view: View, mood: String) {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

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
                Toast.makeText(this, "Selected mood: $mood", Toast.LENGTH_SHORT).show()

                getEmotionOfCurrentUserAndDate(current_user, currentDate) { item ->
                    if (item != null) {
                        updateEmotionInDB(current_user, currentDate, mood)
                    }
                    else {
                        val newMood = EmotionModel(mood, currentDate, current_user)
                        addEmotionToDB(newMood)
                    }
                }
            }
            else {
                view.setBackgroundResource(android.R.color.transparent)
                Toast.makeText(this, "Deselected mood: $mood", Toast.LENGTH_SHORT).show()

                // remove from the database
                getEmotionOfCurrentUserAndDate(current_user, currentDate) { item ->
                    if (item != null) {
                        removeEmotionFromDB(current_user, currentDate)
                    }
                }
            }
        }


        val worstMoodButton: ImageButton = findViewById(R.id.worstMood)
        worstMoodButton.setOnClickListener { onMoodButtonClick(it, "worst") }
        val badMoodButton: ImageButton = findViewById(R.id.badMood)
        badMoodButton.setOnClickListener { onMoodButtonClick(it, "bad") }
        val neutralMoodButton: ImageButton = findViewById(R.id.neutralMood)
        neutralMoodButton.setOnClickListener { onMoodButtonClick(it, "neutral") }
        val goodMoodButton: ImageButton = findViewById(R.id.goodMood)
        goodMoodButton.setOnClickListener { onMoodButtonClick(it, "good") }
        val bestMoodButton: ImageButton = findViewById(R.id.bestMood)
        bestMoodButton.setOnClickListener { onMoodButtonClick(it,"best") }

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
                    // Get all entries of current user and adds them to this.entryList the call setUpArchives
                    getAllEntriesOfCurrentUser(current_user)
                    setupArchives()
                }
            }
            true
        }
    }

    // Gets emotion under the users name and on a specific date from DB
    private fun getEmotionOfCurrentUserAndDate(currentUser: String, date: String, onEmotionLoaded: (EmotionModel?) -> Unit) {
        db.collection(COLLECTION_EMOTIONS)
            .whereEqualTo(FIELD_USER_ID, currentUser)
            .whereEqualTo(FIELD_DATE, date)
            .get()
            .addOnSuccessListener { documents ->
                val emotionModel: EmotionModel? = if (documents.isEmpty) null else {
                    val document = documents.first()
                    val emotionSubmitted = document.get(FIELD_EMO_TRACKED).toString()
                    val emotionUserId = document.get(FIELD_USER_ID).toString()
                    val emotionDate = document.get(FIELD_DATE).toString()
                    EmotionModel(emotionSubmitted, emotionDate, emotionUserId)
                }
                onEmotionLoaded(emotionModel)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_LONG).show()
                onEmotionLoaded(null)
            }
    }

    // Add new mood to the DB
    private fun addEmotionToDB(emotionModel: EmotionModel) {
        val db = FirebaseFirestore.getInstance()

        db.collection(COLLECTION_EMOTIONS)
            .add(emotionModel)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error adding document", error)
            }
    }

    // Updates mood from the DB
    private fun updateEmotionInDB(currentUser: String, date: String, newMood: String) {
        val db = FirebaseFirestore.getInstance()

        val query = db.collection(COLLECTION_EMOTIONS)
            .whereEqualTo(FIELD_USER_ID, currentUser)
            .whereEqualTo(FIELD_DATE, date)

        query.get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val document = documents.first()
                document.reference.update(FIELD_EMO_TRACKED, newMood)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                    }
                    .addOnFailureListener { error ->
                        Log.e(TAG, "Error updating document", error)
                    }
            } else {
                Log.e(TAG, "No document found for user $currentUser on date $date")
            }
        }
        .addOnFailureListener { error ->
            Log.e(TAG, "Error getting documents", error)
        }
    }

    // Remove mood from DB
    private fun removeEmotionFromDB(currentUser: String, date: String) {
        val db = FirebaseFirestore.getInstance()

        val query = db.collection(COLLECTION_EMOTIONS)
            .whereEqualTo(FIELD_USER_ID, currentUser)
            .whereEqualTo(FIELD_DATE, date)

        query.get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val document = documents.first()
                document.reference.delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                    }
                    .addOnFailureListener { error ->
                        Log.e(TAG, "Error deleting document", error)
                    }
            } else {
                Log.e(TAG, "No document found for user $currentUser on date $date")
            }
        }
        .addOnFailureListener { error ->
            Log.e(TAG, "Error getting documents", error)
        }
    }

    // Gets the right image src based on the mood
    private fun getMoodSrc(mood: String): Int {
        return when (mood) {
            "best" -> R.drawable.best_mood_icon
            "good" -> R.drawable.good_mood_icon
            "neutral" -> R.drawable.neutral_mood_icon
            "bad" -> R.drawable.bad_mood_icon
            "worst" -> R.drawable.worst_mood_icon
            else -> 0
        }
    }

    // Gets entries under the users name from DB
    private fun getAllEntriesOfCurrentUser(currentUser : String, entryList: ArrayList<EntryModel> = this.entryList){
        class DBThread() : Runnable {
            override fun run() {
                db.collection(COLLECTION_ENTRIES)
                    .whereEqualTo(FIELD_USER_ID, currentUser)
                    .get()
                    .addOnSuccessListener { documents ->
                        for(document in documents){
                            var entryTitle = document.get(FIELD_ENT_TITLE).toString()
                            var entryBody = document.get(FIELD_ENT_BODY).toString()
                            var entryDate = document.get(FIELD_DATE).toString()
                            var entryImage = document.get(FIELD_ENT_IMG).toString()
                            entryList.add(
                                EntryModel(
                                    entryTitle,
                                    entryBody,
                                    entryDate,
                                    entryImage
                                )
                            )
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: $exception")
                    }
            }
        }
        val runnable = DBThread()
        Thread(runnable).start()
    }



    private fun setupArchives(/*documents: QuerySnapshot*/) {
        // Inflate the Archives layout
        this.viewBinding = ActivityArchivesBinding.inflate(layoutInflater)
        setContentView(this.viewBinding.root)

        recyclerView = viewBinding.recyclerView
        calendarView = viewBinding.calendarView

        // On first open of archives page -- this section doesn't work for some reason
        val selectedDate = LocalDate.now().toString() // returns "year-month-day"
        val currentEntries = this.entryList.filter { it.dateString == selectedDate }
        myAdapter = MyAdapter(currentEntries, viewNoteLauncher)
        recyclerView.adapter = myAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Upon change of date in archive page
        calendarView.setOnDateChangeListener { _, i, i1, i2 ->
            var year = i.toString()
            var month = (i1 + 1).toString()
            var day = i2.toString()
            var newSelectedDate = "$year-$month-$day"
            val currentEntries = this.entryList.filter { it.dateString == newSelectedDate }
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

