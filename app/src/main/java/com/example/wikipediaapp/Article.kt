package com.example.wikipediaapp

import android.os.Parcel
import android.os.Parcelable


/**
 * Class used to store info about article
 */
class Article(var title: String, var snippet: String) : Parcelable {

    // parcelable methods below...
    // ---------------------------------------------------------------------------------------------
    constructor(parcel: Parcel) : this(
        title = parcel.readString(),
        snippet = parcel.readString()
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(snippet)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Article> {
        override fun createFromParcel(parcel: Parcel): Article {
            return Article(parcel)
        }

        override fun newArray(size: Int): Array<Article?> {
            return arrayOfNulls(size)
        }
    }
}