package com.example.myapplication.gallary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
        var pathList = ArrayList<Path>()
        var colorList = ArrayList<Int>()
        var xpathList = ArrayList<Float>()
        var ypathList = ArrayList<Float>()
        var currentBrush = Color.BLACK
    }

    private fun init(){
        paintBrush.isAntiAlias = true
        paintBrush.color = currentBrush
        paintBrush.style = Paint.Style.STROKE
        paintBrush.strokeJoin = Paint.Join.ROUND
        paintBrush.strokeWidth = 8f

        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("PaintView", "onSizeChanged: width=$w, height=$h")
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        var x = event.x
        var y = event.y
        xpathList.add(x)
        ypathList.add(y)

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
        for(i in pathList.indices){
            paintBrush.setColor(colorList[i])
            canvas.drawPath(pathList[i], paintBrush)
            invalidate()
        }

    }

}