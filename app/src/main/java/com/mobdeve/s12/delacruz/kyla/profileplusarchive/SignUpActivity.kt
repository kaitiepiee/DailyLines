package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private companion object {
        const val RC_SIGN_IN = 9001 // Arbitrary request code for Google Sign-In
    }

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_screen)

        mAuth = FirebaseAuth.getInstance()

        // Initialize Google Sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnCreateAccountWithGoogle: SignInButton = findViewById(R.id.sign_up_button)
        btnCreateAccountWithGoogle.setOnClickListener {
            signInWithGoogle()
        }

        val textSignUp: TextView = findViewById(R.id.textSignUp)
        makeSignUpClickable(textSignUp)
    }

    private fun makeSignUpClickable(textView: TextView) {
        // Create a ClickableSpan
        val spannableString = SpannableString(textView.text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openLoginActivity(widget) // go to Login
            }
        }

        val clickableText = "Sign in here"
        val start = spannableString.indexOf(clickableText)
        val end = start + clickableText.length

        spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
        textView.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }

    private fun openLoginActivity(widget: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            } else {
                Log.e("SignupActivity", "Sign-in canceled or failed")
                Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it) }
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "signInResult: failed code=${e.statusCode}")
            Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    val user = mAuth.currentUser

                    // Call addUserToFirestore here after successful sign-in
                    addUserToFirestore(user)

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToFirestore(user: FirebaseUser?) {
        user?.let {
            val db = FirebaseFirestore.getInstance()
            val usersCollection = db.collection("Users")

            // Check if the user ID already exists in Firestore
            usersCollection.document(it.uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // User already exists, you can update or handle accordingly
                        Log.d("Firestore", "User already exists in Firestore")
                    } else {
                        // User does not exist, add the data to Firestore
                        val userData = hashMapOf(
                            "user_id" to it.uid,
                            "profileName" to it.displayName,
                            "email" to it.email,
                            "photoUrl" to it.photoUrl
                            // TODO: if other data is needed, get from here
                        )

                        // Add the data to Firestore
                        usersCollection.document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("Firestore", "User data added successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error adding user data", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error checking user existence", e)
                }
        }
    }
}