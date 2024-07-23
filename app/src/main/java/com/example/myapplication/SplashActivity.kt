package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Login.LoginActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.data.Paints
import com.example.myapplication.data.Pid
import com.example.myapplication.data.Users
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.MainScope


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({   //일정 시간 이후 메인으로 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        },1000) //1초 후 메인화면으로 이동
    }
}