package com.example.myapplication.Login

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.myapplication.NaviActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.kakao.sdk.common.util.Utility


class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")
        //로그인 유저 불러오기
        val pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val loginUser = pref.getString("login_user", "null").toString()
        val loginMethod= pref.getString("login_method","null").toString()

        Log.d("yang","loginUser: $loginUser")

        //데이터 존재 확인을 위해 token(user)의 데이터 스냅샷을 한번 읽어오는 함수
        usersRef.child(loginUser).addListenerForSingleValueEvent(object: ValueEventListener {
            //데이터를 성공적으로 읽어본 경우 바로 홈 으로 이동
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    Log.d("yang","loginUser2: $loginUser")
                    moveActivity(loginUser,loginMethod,1)
                }
            }
            //데이터 읽기가 실패한 경우
            override fun onCancelled(e: DatabaseError) {
                Log.d(TAG, "데이터 호출 실패: $e")
            }
        })
        
        val keyHash = Utility.getKeyHash(this)
        Log.d("keyHash", keyHash)

        //네아로 객체 초기화
        NaverIdLoginSDK.initialize(this, getString(R.string.naver_client_id),
            getString(R.string.naver_client_secret), getString(R.string.naver_client_name))

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {

                //로그인 시 토큰을 가지고 navi로 이동
                moveActivity(NaverIdLoginSDK.getAccessToken(),"Naver",0)
//                binding.tvAccessToken.text = NaverIdLoginSDK.getAccessToken()
//                binding.tvRefreshToken.text = NaverIdLoginSDK.getRefreshToken()
//                binding.tvExpires.text = NaverIdLoginSDK.getExpiresAt().toString()
//                binding.tvType.text = NaverIdLoginSDK.getTokenType()
//                binding.tvState.text = NaverIdLoginSDK.getState().toString()
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(applicationContext,"errorCode:$errorCode, errorDesc:$errorDescription",Toast.LENGTH_SHORT).show()
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        binding.naverLogin.setOnClickListener {
            NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
        }

        binding.kakaoLogin.setOnClickListener {
            // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오계정으로 로그인 실패", error)
                } else if (token != null) {
                    Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")

                    //로그인 시 토큰을 가지고 navi로 이동
                    fetchUid()

                }
            }


            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    } else if (token != null) {
                        Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")

                        //로그인 시 토큰을 가지고 navi로 이동
                        fetchUid()

                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.googleLogin.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // 로그 실패 처리
                Log.e("ggoog", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //로그인 시 토큰을 가지고 navi로 이동
                    moveActivity(idToken,"Google",0)
                } else {
                    // 로그인 실패 처리
                }
            }
    }

    //토큰을 넘긴 채 Navi로 이동
    private fun moveActivity(token: String?, auth: String, flag: Int) {
        //로그인 방법을 SharedPreferences에 저장
        //SharedPreference: 간단한 저장을 위한 안드로이드 API
        val pref = getSharedPreferences("userInfo", MODE_PRIVATE)
        pref.edit().putString("login_method",auth).apply()

        val intent = Intent(this, NaviActivity::class.java)
        intent.putExtra("TOKEN",token)
        intent.putExtra("Auth", auth)
        //0이면 소셜 로그인, 1이면 로그인 전적 있음
        intent.putExtra("flag", flag)
        startActivity(intent)
        finish()
    }

    //카카오톡 로그인 토큰의 고유 UserId를 호출하는 함수
    private fun fetchUid() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                val userId = user.id
                moveActivity(userId.toString(), "Kakao",0)
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}