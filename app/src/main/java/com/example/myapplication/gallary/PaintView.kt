package com.example.myapplication.gallary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet.Motion
import com.example.myapplication.gallary.GallaryFragment.Companion.paintBrush
import com.example.myapplication.gallary.GallaryFragment.Companion.path

class PaintView : View {
    //createView를 코트린 언어로 보여주는 방법
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
    companion object{
        //pathList안에는 사용자가 그린 path를 저장할 예정
        var pathList = ArrayList<Path>()
        //각각 path에 따른 컬러를 저장
        var colorList = ArrayList<Int>()
        //색상 설정 black으로 초기화 (사용자가 사용하는 GallaryFragment딴에서 바꿀 수 있게 하기 위해 companion object로 설정)
        var currentBrush = Color.BLACK
    }

    //초기화 과정
    private fun init(){
        //안티 앨리어싱 활성화, 경계가 더 부드럽게 그려짐
        paintBrush.isAntiAlias = true
        //현재 붓 색상을 설정함 여기선 Color.BLACK임
        paintBrush.color = currentBrush
        //그리기 스타일 여기서는 선 스타일(stroke)로 설정
        paintBrush.style = Paint.Style.STROKE
        //선이 만나는 부분을 둥글게 설정
        paintBrush.strokeJoin = Paint.Join.ROUND
        //선의 너비를 8픽셀로 설정합니다.
        paintBrush.strokeWidth = 8f

        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var x = event.x
        var y = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN->{
                path.moveTo(x,y)
                return true
            }
            MotionEvent.ACTION_MOVE->{
                path.lineTo(x,y)
                pathList.add(path)
                colorList.add(currentBrush)
            }
            else -> return false
        }
        postInvalidate()
        return false;
    }

    override fun onDraw(canvas: Canvas) {
        //배경색 설정
        val paint = Paint()
        paint.setColor(Color.WHITE)
        //하얀색 사각형 넣기 2.54는 dp로 변형하기 위해 사용
        var whiteRect = RectF(0f,0f, 1500f, 540*2.54f)
        canvas.drawRect(whiteRect,paint)
        for(i in pathList.indices){
            paintBrush.setColor(colorList[i])
            //pathList안에 있는 모든 path를 그려줌 각각 paintBrush로
            canvas.drawPath(pathList[i], paintBrush)
            //ondraw쪽으로 가서 그림을 다시 그림
            invalidate()
        }

    }

}