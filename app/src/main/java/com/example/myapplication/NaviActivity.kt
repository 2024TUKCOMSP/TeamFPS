package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.fragment.app.Fragment
import com.example.myapplication.data.Users
import com.example.myapplication.databinding.ActivityNaviBinding
import com.example.myapplication.databinding.CustomDialogBinding
import com.example.myapplication.gallary.GalleryFragment
import com.example.myapplication.home.HomeFragment
import com.example.myapplication.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

interface TokenProvider{
    fun getToken(): String?
}

class NaviActivity : AppCompatActivity() {

    lateinit var binding : ActivityNaviBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //파이어베이스 초기화
        FirebaseApp.initializeApp(this)

        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView =findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val profileFragment = ProfileFragment()
        val homeFragment = HomeFragment()
        val galleryFragment = GalleryFragment()
        replaceFragment(homeFragment)
        bottomNavigationView.selectedItemId = R.id.home


        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.profile -> replaceFragment(profileFragment)
                R.id.home -> replaceFragment(homeFragment)
                R.id.draw-> replaceFragment(galleryFragment)
            }
            true
        }

        //로그인 토큰 받기
        val token = intent.getStringExtra("TOKEN")
        val auth = intent.getStringExtra("Auth")
        val checkFlag = intent.getIntExtra("flag",111)

        //fragment로 uid 데이터 전달
        if (token != null) {
            val uid: String = if(checkFlag==0) {
                getUidFromToken(token)
            } else token
            val bundle = Bundle()
            bundle.putString("UID", uid)
            homeFragment.arguments = bundle
        }

        //데이터 존재 확인
        checkUser(token, auth, checkFlag)
    }
    fun getToken(): String?{
        return intent.getStringExtra("TOKEN")
    }

    //데이터 존재 확인
    private fun checkUser(token: String?, auth: String?, checkFlag: Int){
        if (token== null) return

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")
        //token을 해쉬 값으로 변환
        val uid: String = if(checkFlag==0) {
            getUidFromToken(token)
        } else token

        //token을 sharedPreference에 저장
        val pref = getSharedPreferences("userInfo", MODE_PRIVATE)
        pref.edit().putString("login_user",uid).apply()

        //데이터 존재 확인을 위해 token(user)의 데이터 스냅샷을 한번 읽어오는 함수
        usersRef.child(uid).addListenerForSingleValueEvent(object: ValueEventListener {
            //데이터를 성공적으로 읽어본 경우
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    //DB에 없는 경우 회원가입
                    showSignUpDialog(uid,auth)
                }
            }
            //데이터 읽기가 실패한 경우
            override fun onCancelled(e: DatabaseError) {
                Log.d("yang", "데이터 호출 실패: $e")
            }
        })
    }

    //커스텀 다이얼로그 설정
    private fun showSignUpDialog(uid: String, auth: String?){
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
            //텍스트가 변경되면 호출
            override fun afterTextChanged(s: Editable?) {
                val name = dialogBinding.setName.text.toString().trim()
                val nickname = dialogBinding.setNickname.text.toString().trim()
                //이름과 닉네임이 비어있지 않을 때 완료버튼 활성화
                positiveBtn.isEnabled = name.isNotEmpty() && nickname.isNotEmpty()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        dialogBinding.setName.addTextChangedListener(textWatcher)
        dialogBinding.setNickname.addTextChangedListener(textWatcher)

        val imgref = FirebaseStorage.getInstance().reference.child("OG.png")

        //완료버튼 누를 시 회원가입
        positiveBtn.setOnClickListener {
            val name = dialogBinding.setName.text.toString()
            val nickname = dialogBinding.setNickname.text.toString()

            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("users")

            var imgURL: String? = ""

            val profileRef = FirebaseStorage.getInstance().reference.child("profile_images/$uid")

            val bitmap = BitmapFactory.decodeResource(resources,R.drawable.og)
            val file = File(cacheDir,"og.png")

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            val uri = Uri.fromFile(file)

            profileRef.putFile(uri).addOnSuccessListener {
                profileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    imgURL = downloadUri.toString()
                        //회원정보 클래스 생성
                    val user = Users(uid,name,nickname,imgURL, auth, "10000")
                        //db에 회원정보 저장
                    usersRef.child(user.uid!!).setValue(user)
                }
            }.addOnFailureListener{ exception->
                Log.e("yang", "profileRef 오류.", exception)
            }

            //캐시 삭제
            file.delete()
            alertDialog.dismiss()


        }

    }

    private fun getUidFromToken(token: String): String{
        return token.hashCode().toString()
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
        invalidateOptionsMenu()
    }
}