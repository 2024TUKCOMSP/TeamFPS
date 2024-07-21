package com.example.myapplication.gallary

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.EditText
import com.example.myapplication.NaviActivity
import com.example.myapplication.R
import com.example.myapplication.data.Paints
import com.example.myapplication.data.Pid
import com.example.myapplication.databinding.ActivitySelectDrawingCostNameBinding
import com.example.myapplication.gallary.PaintView.Companion.colorList
import com.example.myapplication.gallary.PaintView.Companion.xpathList
import com.example.myapplication.gallary.PaintView.Companion.ypathList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SelectDrawingCostName(context: Context) : AlertDialog(context){

    // Fragment에서 dialog받아오는 코드
    private val dialogBuilder = AlertDialog.Builder(context)
    // dialogActivity 를 바인딩 하는 코드
    private val dialogBinding: ActivitySelectDrawingCostNameBinding
    // context를 이용하여 R.layout.dialog xml 파일을 인플레이트 시키는 코드 즉 뷰 객체 생성
    private val rootView = View.inflate(context,R.layout.activity_select_drawing_cost_name, null)
    //AlertDialog(다이어그램) 우리는 커스터을 할꺼기 때문에 받아온다.
    private var dialog: AlertDialog? = null


    //init코드 view로 화면을 띄운다.
    init {
        val rootView = View.inflate(context, R.layout.activity_select_drawing_cost_name, null)
        dialogBinding = ActivitySelectDrawingCostNameBinding.bind(rootView)
        dialogBuilder.apply {
            setView(rootView)
        }
    }
    //기능 함수 간단히 주변 투명화, 확인 취소 버튼 넣음
    override fun show() {
        //다이어그램 창 띄우기
        dialog = dialogBuilder.create()
        //주변투명화
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //yes버튼 기능
        dialogBinding.yesButton.setOnClickListener {
            // 확인 버튼 클릭 시 처리할 로직을 여기에 추가
            val cost = dialogBinding.picturecost.text.toString()
            val name = dialogBinding.picturename.text.toString()
            //데베 연결
            val database = FirebaseDatabase.getInstance()
            //그림 데베 정보 접근
            val paintsRef = database.getReference("paint")
            //pid 데베 정보 접근
            val pidRef = database.getReference("pid")
            pidRef.child("1").addListenerForSingleValueEvent(object: ValueEventListener {
                //데이터를 성공적으로 읽어본 경우
                override fun onDataChange(snapshot: DataSnapshot) {
                    val retrievedPid = snapshot.getValue(Pid::class.java)
                    //Log.d("yang","token2")
                    if (snapshot.exists()) {
                        var Intpid = retrievedPid?.pid?.plus(1)
                        val setpid = Pid(Intpid)
                        pidRef.child("1").setValue(setpid)

                        val paint = Paints(Intpid.toString(),null,cost.toString(),name.toString(),null, xpathList, ypathList, colorList)
                        paintsRef.child(paint.pid!!).setValue(paint)
                    }
                }

                //데이터 읽기가 실패한 경우
                override fun onCancelled(e: DatabaseError) {
                    Log.d("yang", "데이터 호출 실패: $e")
                }
            })

            dialog?.dismiss() // 다이얼로그 닫기
        }
        // 취소 버튼 클릭 시 처리
        dialogBinding.noButton.setOnClickListener {
            dialog?.dismiss() // 다이얼로그 닫기
        }

        dialog?.show()
        // 확인 버튼 클릭 시 처리할 로직을 여기에 추가

    }
}