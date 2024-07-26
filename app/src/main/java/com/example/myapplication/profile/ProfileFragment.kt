package com.example.myapplication.profile

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.myapplication.Login.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.data.Users
import com.example.myapplication.databinding.ChangeProfileBinding
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
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK

class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var imageUri: Uri? = null
    private lateinit var loginUser : String
    private lateinit var dialogBinding: ChangeProfileBinding

    // ActivityResultLauncher를 클래스 멤버로 선언
    private lateinit var requestLauncher: ActivityResultLauncher<Intent>


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

        // ActivityResultLauncher 초기화
        requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                // 다이얼로그의 ImageView에 이미지 표시
                if (this::dialogBinding.isInitialized) {
                    Glide.with(this)
                        .load(imageUri)
                        .into(dialogBinding.profileImage)
                }
            }
        }

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
        loginUser = pref.getString("login_user", "").toString()

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
                showChangeProfileDialog()
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

    private fun showChangeProfileDialog(){
        dialogBinding = ChangeProfileBinding.inflate(layoutInflater)
        //다이얼로그 설정
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("프로필 변경")
            .setView(dialogBinding.root)
            .setPositiveButton("완료",null)

        val alertDialog = builder.create()  //다이얼로그 생성
        alertDialog.show()  //다이얼로그 띄우기

        //갤러리에서 이미지 선택
        dialogBinding.imageToGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            requestLauncher.launch(intent)
        }

        //완료버튼 id
        val positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveBtn.setOnClickListener{
            val nickname = dialogBinding.setNickname.text.toString()
            if(imageUri != null && nickname.isNotEmpty()){
                updateProfile(nickname)
                alertDialog.dismiss()
            }
            else
                Toast.makeText(requireContext(), "이미지 혹은 닉네임변경부분이 비어있습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(nickname: String){
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_images/$loginUser")
        Log.d("yang","updateProfileTest: $imageUri")

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users").child(loginUser)

        //닉네임 변경
        usersRef.child("nickname").setValue(nickname)


        imageRef.putFile(imageUri!!).addOnSuccessListener{
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                usersRef.child("profilePictureUrl").setValue(downloadUri.toString())
            }
            }
            .addOnFailureListener{exception ->
                Log.e("yang", "이미지 업로드 실패", exception)
                Toast.makeText(requireContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show()

            }
    }
    private fun loadUserInfo() {
        //현재 로그인한 유저 객체
        //val user = auth.currentUser
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users").child(loginUser)

        usersRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue(Users::class.java)
                Log.d("yang", "loadUserInfo $userProfile")
                if (userProfile!=null && isAdded){  // Fragment가 Activity에 연결되어 있는지 확인
                    binding.nameText.text = userProfile.nickname
                    //이미지 호출
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