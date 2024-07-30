package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Login.LoginActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.data.Paints
import com.example.myapplication.data.Pid
import com.example.myapplication.data.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kakao.sdk.user.Constants.TAG
import kotlinx.coroutines.MainScope


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")
        //로그인 유저 불러오기
        val pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val loginUser = pref.getString("login_user", "null").toString()
        val loginMethod= pref.getString("login_method","null").toString()

        //데이터 존재 확인을 위해 token(user)의 데이터 스냅샷을 한번 읽어오는 함수
        usersRef.child(loginUser).addListenerForSingleValueEvent(object: ValueEventListener {
            //데이터를 성공적으로 읽어본 경우 바로 홈 으로 이동
            override fun onDataChange(snapshot: DataSnapshot) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if(snapshot.exists()){
                        val intent = Intent(this@SplashActivity, NaviActivity::class.java)
                        intent.putExtra("TOKEN",loginUser)
                        intent.putExtra("Auth", loginMethod)
                        //0이면 소셜 로그인, 1이면 로그인 전적 있음
                        intent.putExtra("flag", 1)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                },1000)

            }
            //데이터 읽기가 실패한 경우
            override fun onCancelled(e: DatabaseError) {
                Log.d(TAG, "데이터 호출 실패: $e")
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }, 1000)
            }
        })

    }
}