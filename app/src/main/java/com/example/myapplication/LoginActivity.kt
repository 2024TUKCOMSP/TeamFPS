package com.example.myapplication

import android.content.ContentValues.TAG
import android.content.Intent
import android.credentials.GetCredentialException
import android.credentials.GetCredentialRequest
import android.credentials.GetCredentialResponse
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CustomCredential
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
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
        enableEdgeToEdge()
        setContentView(binding.root)

        var keyHash = Utility.getKeyHash(this)
        Log.d("keyHash", keyHash)

        //네아로 객체 초기화
        NaverIdLoginSDK.initialize(this, getString(R.string.naver_client_id),
            getString(R.string.naver_client_secret), getString(R.string.naver_client_name))

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
                Log.d("naver", "login")
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

        binding.naverLogout.setOnClickListener {
            NaverIdLoginSDK.logout()
        }

        binding.naverDelete.setOnClickListener {
            NidOAuthLogin().callDeleteTokenApi(object : OAuthLoginCallback {
                override fun onSuccess() {
                    Log.d("naver", "delete")
                    //서버에서 토큰 삭제에 성공한 상태입니다.
                }
                override fun onFailure(httpStatus: Int, message: String) {
                    // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                    // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                    Log.d(TAG, "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                    Log.d(TAG, "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
                }
                override fun onError(errorCode: Int, message: String) {
                    // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                    // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                    onFailure(errorCode, message)
                }
            })
        }

        binding.kakaoLogin.setOnClickListener {
            // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오계정으로 로그인 실패", error)
                } else if (token != null) {
                    Log.d("kakao", "login")
                    Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                    //로그인 시 토큰을 가지고 navi로 이동
                    //moveActivity(token.accessToken)
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
                        Log.d("kakao", "login")
                        Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                        //로그인 시 토큰을 가지고 navi로 이동
                        //moveActivity(token.accessToken)
                        fetchUid()

                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

        binding.kakaoLogout.setOnClickListener {
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

        binding.kakaoDelete.setOnClickListener {
            UserApiClient.instance.unlink { error ->
                if (error != null) {
                    Log.d("kakao", "delete fail")
                    Log.e(TAG, "연결 끊기 실패", error)
                }
                else {
                    Log.d("kakao", "delete success")
                    Log.i(TAG, "연결 끊기 성공. SDK에서 토큰 삭제 됨")
                }
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

        binding.googleLogout.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut().addOnCompleteListener(this) {
                // 로그아웃이 완료되면 추가 작업 (예: 로그인 화면으로 이동)
            }
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
                Log.d("ggoog", "login")
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
                    val user = auth.currentUser
                    // 로그인 성공 처리

                    //로그인 시 토큰을 가지고 navi로 이동
                    moveActivity(idToken,"Google")

                    Log.d("ggoog", "login")

                } else {
                    // 로그인 실패 처리
                }
            }
    }

    //토큰을 넘긴 채 Navi로 이동
    private fun moveActivity(token: String?, Auth: String) {
        val intent = Intent(this, NaviActivity::class.java)
        intent.putExtra("TOKEN",token)
        intent.putExtra("Auth", Auth)
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
                Log.i("yang", "사용자 정보 요청 성공. 사용자 ID: $userId")
                moveActivity(userId.toString(), "Kakao")
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}