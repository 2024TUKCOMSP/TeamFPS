package com.example.myapplication.profile

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.bumptech.glide.Glide
import com.example.myapplication.Login.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.data.Users
import com.example.myapplication.databinding.FragmentProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import com.navercorp.nid.NaverIdLoginSDK
import java.util.prefs.Preferences

class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.viewpager.adapter = PagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabs, binding.viewpager){ tab, position ->
            if (position == 0) tab.text = "Draw"
            else tab.text = "Buy"
        }.attach()

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        NaverIdLoginSDK.initialize(requireContext(), getString(R.string.naver_client_id),
            getString(R.string.naver_client_secret), getString(R.string.naver_client_name))

        //툴바 세팅
        val toolbar: Toolbar = binding.myPageToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        setHasOptionsMenu(true)

        //로그인 유저 불러오기
        val pref = requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val loginUser = pref.getString("login_user", "")

        loadUserInfo()

        return binding.root
    }

    //옵션 메뉴를 생성하는 함수
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_profile, menu)
    }

    //메뉴 선택 기능 구현하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_name -> {

                true
            }
            R.id.menu_logout ->{
                val pref = requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
                val loginMethod = pref.getString("login_method","")
                //각 로그인 방식에 따라 로그아웃 방식 분류
                when(loginMethod){
                    "Google" -> {
                        //Firebase 로그아웃
                        auth.signOut()
                        //구글 로그아웃
                        googleSignInClient.signOut().addOnCompleteListener(requireActivity()){
                            Log.d("yang", "구글 로그아웃 완료")
                        }
                    }
                    "Naver" -> {
                        NaverIdLoginSDK.logout()
                        Log.d("yang", "네이버 로그아웃 완료")
                    }
                    "Kakao" ->{
                        UserApiClient.instance.logout { error ->
                            if (error != null) {
                                Log.d("kakao", "logout fail")
                                Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                            }
                            else {
                                Log.d("kakao", "logout success")
                                Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                            }
                        }
                    }
                    else -> {
                        //Firebase 로그아웃
                        auth.signOut()
                    }
                }
                // 로그아웃 시 SharedPreferences에 저장된 로그인 방법 삭제
                pref.edit().remove("login_method").apply()

                val intent = Intent(requireActivity(), LoginActivity::class.java)
                // CLEAR_TOP : 액티비티 스택을 모두 삭제, NEW_TASK: 액티비티 실행 시 새 task에서 액티비티 실행
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUserInfo(){

        //현재 로그인한 유저 객체
        val user = auth.currentUser
        if(user!=null){
            Log.d("yang", "Current user UID: ${user.uid}")
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("users").child(user.uid)

            usersRef.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfile = snapshot.getValue(Users::class.java)
                    Log.d("yang", "loadUserInfo $userProfile")
                    if (userProfile!=null){
                        binding.nameText.text = userProfile.nickname
                        Glide.with(this@ProfileFragment)
                            .load(userProfile.profilePictureUrl)
                            .into(binding.profileImg)
                    }
                    else
                        Log.d("yang", "Current user is null")
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("yang", "유저 프로필 로드 실패", error.toException())
                }
            })
        }
        else
            Log.d("yang", "Current user is null")
    }
}


// 어댑터 클래스
class PagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    //프래그먼트 리스트 선언 및 초기화
    val fragments: List<Fragment>
    init {
        fragments = listOf(DrawFragment(), BuyFragment())
    }
    // 프래그먼트 리스트의 크기 반환
    override fun getItemCount(): Int = fragments.size
    //프래그먼트 생성
    override fun createFragment(position: Int): Fragment = fragments[position]
}