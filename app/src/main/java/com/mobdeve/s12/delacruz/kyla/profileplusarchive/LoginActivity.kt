package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

/**
 * This class shows the login feature of the application
 * using Google Firebase's Authentication & Firestore Database
 */
class LoginActivity : AppCompatActivity() {

    private companion object {
        const val RC_SIGN_IN = 9001 // Arbitrary request code for Google Sign-In
    }

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        Log.e("LoginActivity", "Initializing")

        // Initialize Firebase and Google Sign in
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignInManager.getGoogleSignInClient(this)

        // Check if the user is already signed in
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            // User is already signed in, redirect to the main activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val signInButton: View = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener {
            signInWithGoogle()
        }
    }



    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            } else {
                // Case when the sign-in is canceled or failed
                Log.e("LoginActivity", "Sign-in canceled or failed")
                Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }

    private fun signInWithGoogle() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            // Sign out the current user if one exists
            mAuth.signOut()
            mGoogleSignInClient.signOut().addOnCompleteListener(this) {

                // After signing out, proceed with the new sign-in
                startGoogleSignIn()
            }
        } else {
            // If no current user, proceed with the new sign-in
            startGoogleSignIn()
        }
    }

    private fun startGoogleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, authenticate with Firebase
            account?.let { firebaseAuthWithGoogle(it) }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w("GoogleSignIn", "signInResult: failed code=${e.statusCode}")
            Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    val user = mAuth.currentUser

                    // Check if the user exists in Firestore
                    checkUserInFirestore(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserInFirestore(user: com.google.firebase.auth.FirebaseUser?) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("Users")

        // Check if the user ID exists in Firestore
        usersCollection.document(user?.uid ?: "")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // User exists, go to the main activity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // User does not exist, go to the signup activity
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error checking user existence", e)
                Toast.makeText(this, "Error checking user existence", Toast.LENGTH_SHORT).show()
            }
    }
}