package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.data.Users
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({   //일정 시간 이후 메인으로 이동
            startActivity(Intent(this, NaviActivity::class.java))
            finish()
        },1000) //1초 후 메인화면으로 이동

        /*//DB저장 테스트
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        val user1 = Users("user1","DongGyun Yang", "ydg0724@gmail.com","http://example.com/john.jpg","google")
        val user2 = Users("user2","DongGyun Lee", "ydg0724@naver.com","http://example.com/Lee.jpg","Naver")
        val user3 = Users("user3","DongGyun Kim", "ydg0724@kakao.com","http://example.com/Kim.jpg","Kakao")

        usersRef.child(user1.uid!!).setValue(user1)
        usersRef.child(user2.uid!!).setValue(user2)
        usersRef.child(user3.uid!!).setValue(user3)*/
    }
}