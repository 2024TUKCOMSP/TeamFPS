package com.example.myapplication.data

data class Users(
    var uid: String? = "",
    var name: String? = "",
    var nickname: String? = "",
    var profilePictureUrl: String? = "",
    var authprovider: String? ="",
    var mileage: Int?
)
//Canvas<-