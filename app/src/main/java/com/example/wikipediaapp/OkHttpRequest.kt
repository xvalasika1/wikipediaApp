package com.example.wikipediaapp

import okhttp3.*


/**
 * Class used to send http request, using OkHttp library
 */
class OkHttpRequest(private var client: OkHttpClient) {

    /**
     * Method that is used to build the http get request from parameters
     *
     * @param   url                 url to be requested from
     * @param   callback            callback used after the request
     * @return                      the request call is returned
     */
    fun httpGet(url: String, callback: Callback): Call {
        val request = Request.Builder()
                .url(url)
                .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

}