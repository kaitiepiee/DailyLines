package com.mobdeve.s12.delacruz.kyla.profileplusarchive

interface QuotesResponseListener {
    fun didFetch(response: List<QuotesResponse>?, message: String, message1: String)
    fun didError(message: String)
}