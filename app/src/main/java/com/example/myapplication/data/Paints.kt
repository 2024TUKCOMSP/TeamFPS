package com.example.myapplication.data

data class Paints(
    var pid: String? = "",//그림 ID
    var uid: String? = "",//사용자 ID
    var cost: String? = "", //마일리지 가격
    var name: String? = "", //그림 이름
    var sell: Int? = 0,
    var drawURL: String? = "",
    var owner: String? = ""
)