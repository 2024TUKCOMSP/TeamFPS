package com.example.myapplication

import android.app.ProgressDialog.show
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.myapplication.data.Users
import com.example.myapplication.databinding.ActivityNaviBinding
import com.example.myapplication.databinding.CustomDialogBinding
import com.example.myapplication.gallary.GallaryFragment
import com.example.myapplication.home.HomeFragment
import com.example.myapplication.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kakao.sdk.user.model.User

class NaviActivity : AppCompatActivity() {

    lateinit var binding : ActivityNaviBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bottomNavigationView =findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val profileFragment = ProfileFragment()
        val homeFragment = HomeFragment()
        val gallaryFragment = GallaryFragment()
        replaceFragment(homeFragment)
        bottomNavigationView.selectedItemId = R.id.home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.profile -> replaceFragment(profileFragment)
                R.id.home -> replaceFragment(homeFragment)
                R.id.draw-> replaceFragment(gallaryFragment)
            }
            true
        }

        //로그인 토큰 받기
        val token = intent.getStringExtra("TOKEN")

        showSignUpDialog(token)
    }

    //커스텀 다이얼로그 설정
    private fun showSignUpDialog(token: String?){
        //커스텀 다이얼로그의 뷰바인딩 설정
        val dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        //다이얼로그 설정
        val builder = AlertDialog.Builder(this)
            .setTitle("회원가입")
            .setView(dialogBinding.root)
            .setPositiveButton("완료",null)

        val alertDialog = builder.create()  //다이얼로그 생성
        alertDialog.show()  //다이얼로그 띄우기
        alertDialog.setCancelable(false)    //다이얼로그 외부 클릭 시 종료 방지

        //완료버튼 id
        val positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveBtn.isEnabled = false

        // 텍스트 필드 변경 감지 및 버튼 활성화 상태 제어
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val name = dialogBinding.setName.text.toString().trim()
                val nickname = dialogBinding.setNickname.text.toString().trim()
                positiveBtn.isEnabled = name.isNotEmpty() && nickname.isNotEmpty()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        dialogBinding.setName.addTextChangedListener(textWatcher)
        dialogBinding.setNickname.addTextChangedListener(textWatcher)

        positiveBtn.setOnClickListener {
            val name = dialogBinding.setName.text.toString()
            val nickname = dialogBinding.setNickname.text.toString()

            val Database = FirebaseDatabase.getInstance()
            val usersRef = Database.getReference("users")

            val tokens = "abcdfe"
            val user = Users(tokens,name,nickname,null)

            usersRef.child(user.uid!!).setValue(user)

            alertDialog.dismiss()
        }

    }


    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
    }
}