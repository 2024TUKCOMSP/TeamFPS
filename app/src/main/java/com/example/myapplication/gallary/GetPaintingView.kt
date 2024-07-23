package com.example.myapplication.gallary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.data.Paints
import com.example.myapplication.gallary.GallaryFragment.Companion.paintBrush
import com.example.myapplication.gallary.PaintView.Companion.colorList
import com.example.myapplication.gallary.PaintView.Companion.pathList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.abs
import kotlin.math.absoluteValue

class GetPaintingView : View {
    constructor(context: Context) : this(context, null){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    var params : ViewGroup.LayoutParams? = null


    companion object {
        var seePathList = ArrayList<Path>()
        var seeColorList = ArrayList<Int>()
    }

    private fun init(){
        paintBrush.isAntiAlias = true
        paintBrush.color = PaintView.currentBrush
        paintBrush.style = Paint.Style.STROKE
        paintBrush.strokeJoin = Paint.Join.ROUND
        paintBrush.strokeWidth = 8f

        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        fetchData()
    }

    private fun fetchData() {
        val database = FirebaseDatabase.getInstance()
        val paintsRef = database.getReference("paint")
        paintsRef.child("11").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val retrievedPaint = snapshot.getValue(Paints::class.java)
                if (snapshot.exists() && retrievedPaint != null) {
                    seeColorList = retrievedPaint.cPath?.toList() as ArrayList<Int>
                    val seeYpathList = retrievedPaint.yPath?.toList() as ArrayList<Float>
                    val seeXpathList = retrievedPaint.xPath?.toList() as ArrayList<Float>
                    Log.d("drawing...", "onDraw 호출됨: seePathList.size=${seeXpathList}, seeColorList.size=${seeYpathList}")
                        if (seeXpathList != null) {
                            if (seeYpathList != null && seeXpathList != null && seeColorList.isNotEmpty()) {
                                seePathList.clear()
                                // 새로운 Path를 생성하고 좌표를 추가합니다.
                                var currentPath = Path()
                                for (i in seeColorList.indices) {
                                    if (i == 0 || (abs(seeXpathList[i] - seeXpathList[i - 1])>=100 && abs(seeYpathList[i]-seeYpathList[i-1]) >=100)) {
                                        // 새로운 색상 시작 시 새로운 Path를 생성
                                        currentPath.moveTo(seeXpathList[i], seeYpathList[i])
                                        seePathList.add(currentPath)
                                    } else {
                                        currentPath.lineTo(seeXpathList[i], seeYpathList[i])
                                    }
                                }
                                invalidate()
                            }
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("drawing...", "데이터 호출 실패: $error")
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("drawing...", "onDraw 호출됨: seePathList.size=${seePathList}")
        Log.d("drawing...", "onDraw 호출됨: seeColorList.size=${seeColorList}")

        for (i in seePathList.indices) {
            paintBrush.setColor(seeColorList[i])
            canvas.drawPath(seePathList[i], paintBrush)
        }
    }

}