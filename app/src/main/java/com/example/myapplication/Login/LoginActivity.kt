package com.example.myapplication.Login

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.NaviActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

        val keyHash = Utility.getKeyHash(this)
        Log.d("keyHash", keyHash)

        //네아로 객체 초기화
        NaverIdLoginSDK.initialize(this, getString(R.string.naver_client_id),
            getString(R.string.naver_client_secret), getString(R.string.naver_client_name))

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {

                //로그인 시 토큰을 가지고 navi로 이동
                moveActivity(NaverIdLoginSDK.getAccessToken(),"Naver")
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
                    moveActivity(idToken,"Google")
                } else {
                    // 로그인 실패 처리
                }
            }
    }

    //토큰을 넘긴 채 Navi로 이동
    private fun moveActivity(token: String?, auth: String) {
        //로그인 방법을 SharedPreferences에 저장
        //SharedPreference: 간단한 저장을 위한 안드로이드 API
        val pref = getSharedPreferences("userInfo", MODE_PRIVATE)
        pref.edit().putString("login_method",auth).apply()

        val intent = Intent(this, NaviActivity::class.java)
        intent.putExtra("TOKEN",token)
        intent.putExtra("Auth", auth)
        startActivity(intent)
        finish()
    }

    //카카오톡 로그인 토큰의 고유 UserId를 호출하는 함수
    private fun fetchUid() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("yang", "사용자 정보 요청 실패", error)
            } else if (user != null) {
                val userId = user.id
                moveActivity(userId.toString(), "Kakao")
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}