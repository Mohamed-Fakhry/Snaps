package com.example.computec.testfirrebase

import java.util.*

/**
 * Created by Mohamed Fakhry on 15/11/2017.
 */
data class SportLifeFeed(var key: String?,var userId: String?, var imageUrl: String?,
                         var likes: Int?,
                         var superLikes: Int?,
                         var shares: Int?) {
    constructor() : this(null, null, null, null, null, null)
}

data class Like(var userId: String, var date: Date)

data class Share(var userId: String, var date: Date)

data class User(var id: String?, var name: String, var age: Int, var like: Date)