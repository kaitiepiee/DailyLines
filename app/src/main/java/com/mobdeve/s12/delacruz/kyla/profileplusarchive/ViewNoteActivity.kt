package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.databinding.ActivityViewNoteBinding

class ViewNoteActivity : AppCompatActivity() {
    companion object {
        const val titleKey : String = "TITLE_KEY"
        const val bodyKey : String = "BODY_KEY"
        const val positionKey: String = "POSITION_KEY"
    }

    private lateinit var titleString: String
    private lateinit var bodyString: String
    private lateinit var viewBinding: ActivityViewNoteBinding   // Holds the views of the ActivityViewNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.viewBinding = ActivityViewNoteBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

//        viewBinding.saveBt.isEnabled = false

        // gets the title and body objects from the detail view
        val detailTitle = viewBinding.titleEtv
        val detailBody = viewBinding.bodyEtv

        // get the values passed by intent that correspond to title, body, and position and set title in the detail view
        val titleData = intent.getStringExtra(titleKey)
        val position = intent.getIntExtra(positionKey, 0)
        detailTitle.text = titleData
        val bodyData = intent.getStringExtra(bodyKey)
        detailBody.text = bodyData

        // save as strings
        titleString = titleData.toString()
        bodyString = bodyData.toString()


//        //check if title was edited
//        viewBinding.titleEtv.addTextChangedListener(object : TextWatcher {
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                viewBinding.saveBt.isEnabled = !isTextStillOriginal()
//            }
//            override fun beforeTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//            }
//            override fun afterTextChanged(s: Editable) {
//            }
//        })
//        //check if body was edited
//        viewBinding.bodyEtv.addTextChangedListener(object : TextWatcher {
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                viewBinding.saveBt.isEnabled = !isTextStillOriginal()
//            }
//            override fun beforeTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//            }
//            override fun afterTextChanged(s: Editable) {
//            }
//        })
//        //on click of save button, send data to main activity
//        viewBinding.saveBt.setOnClickListener {
//            val intent = Intent()
//            intent.putExtra(titleKey, viewBinding.titleEtv.text.toString())
//            intent.putExtra(bodyKey, viewBinding.bodyEtv.text.toString())
//            intent.putExtra(positionKey, position)
//            setResult(Activity.RESULT_OK, intent)
//            finish()
//        }
    }

//    private fun isTextStillOriginal() : Boolean {
//        return (this.viewBinding.titleEtv.text.toString() == titleString) and (this.viewBinding.bodyEtv.text.toString() == bodyString)
//    }
}