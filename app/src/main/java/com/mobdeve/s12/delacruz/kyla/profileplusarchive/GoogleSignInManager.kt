package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

object GoogleSignInManager {
    private var mGoogleSignInClient: GoogleSignInClient? = null

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        if (mGoogleSignInClient == null) {
            // Initialize the GoogleSignInOptions
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            // Create the GoogleSignInClient
            mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
        }

        return mGoogleSignInClient!!
    }
}