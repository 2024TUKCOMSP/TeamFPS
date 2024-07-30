package com.example.myapplication.gallary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.gallary.GalleryFragment.Companion.paintBrush
import com.example.myapplication.gallary.GalleryFragment.Companion.path

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
    //기능 추가를 위한 enum class 각각 PATH, LINE, RECTANGLE, CIRCLE
    enum class Shape {
        PATH, LINE, RECTANGLE, CIRCLE
    }

    var params : ViewGroup.LayoutParams? = null
    companion object{
        //pathList안에는 사용자가 그린 path를 저장할 예정
        var pathList = ArrayList<Path>()
        //각각 path에 따른 컬러를 저장
        var colorList = ArrayList<Int>()
        //shapeList로 Shape에 따른 그림을 그릴 수 있게함
        var shapeList = ArrayList<Shape>()
        //색상 설정 black으로 초기화 (사용자가 사용하는 GallaryFragment딴에서 바꿀 수 있게 하기 위해 companion object로 설정)
        var currentBrush = Color.BLACK
        //선 사각형 원형 등의 기능을 추가 하기 위한 enum 열거형 추가
        var currentShape = Shape.PATH
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
    private var startX = 0f
    private var startY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //onTouchEvent는 실시간으로 사람이 터치 할때나 움직일때를 구분해서 좌표를 받아오다.
        //각각 받아온 x,y를 받아옴
        var x = event.x
        var y = event.y
        //action(터치가 감지 되었을때)
        when(event.action){
            //그 액션이 ACTIOM_DOWN(터치라면 새로운 좌표)
            MotionEvent.ACTION_DOWN->{
                startX =x
                startY =y
                //path에 moveTo로 받아오고 추가 ++currntShape가 PATH라면
                if (currentShape == Shape.PATH) {
                    path.moveTo(x, y)
                }
                return true
            }
            //그 액선이 ACTION_MOVE(연속되는 그리기(드래그)라면)
            MotionEvent.ACTION_MOVE->{
                if(currentShape ==Shape.PATH){
                    //path에 lineTo로 받아옴
                    path.lineTo(x,y)
                    //이때 path에 경로를 추가 해 주고(실제 그려지는 시작점)
                    pathList.add(path)
                    //이때 그림의 리스트도 추가해 줌
                    colorList.add(currentBrush)
                    //이때 shapeList에도 추가해 줌
                    shapeList.add(currentShape)
                }
            }
            //손을 땐 순간을 뜻함 (드래그해서 때는 순간
            MotionEvent.ACTION_UP -> {
                when (currentShape) {
                    //고른게 선이라면
                    Shape.LINE -> {
                        //먼저 경로 시작을 이렇게 옮겨주고
                        path.moveTo(startX, startY)
                        //손을 땐 순간 lineto를 이용하여 선을 그려줌
                        path.lineTo(x, y)
                        //path에 추가 및 각각 color, shape List에 추가
                        pathList.add(path)
                        colorList.add(currentBrush)
                        shapeList.add(currentShape)
                    }
                    //고른게 사각형이라면
                    Shape.RECTANGLE -> {
                        //경로에 추가해준다. 여기서 Path.Direction.CW는 그림을 시계방향으로 그린다는 뜻으로 중요한 코드는 아니다.
                        path.addRect(startX, startY, x, y, Path.Direction.CW)
                        //path에 추가 및 각각 color, shape List에 추가
                        pathList.add(path)
                        colorList.add(currentBrush)
                        shapeList.add(currentShape)
                    }
                    //고른게 원이라면
                    Shape.CIRCLE -> {
                        //수학계산을 해준다.
                        val radius = Math.sqrt(
                            //현재 땐 상태에서의 x값을 원래 초기 x값 및 y값에서 빼준후에 제곱해준 후에 더해준 후에 루트를 씌운다.(피타고라스)
                            Math.pow(
                                (x - startX).toDouble(),
                                2.0
                            ) + Math.pow((y - startY).toDouble(), 2.0)
                        ).toFloat()
                        //나온 반지름으로 원을 그려주는데 사용자의 인식에 맞춰서 그려주는 수식 또한 경로 추가
                        path.addCircle(startX/2+x/2, startY/2+y/2, (radius*Math.sqrt(2.0)/4).toFloat(), Path.Direction.CW)
                        pathList.add(path)
                        colorList.add(currentBrush)
                        shapeList.add(currentShape)
                    }
                    else -> {}
                }

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
        var whiteRect = RectF(0f,0f, 1500f, 700*2.54f)
        canvas.drawRect(whiteRect,paint)
        for(i in pathList.indices){
            paintBrush.setColor(colorList[i])
            //pathList안에 있는 모든 path를 그려줌 각각 paintBrush로
            when (shapeList[i]) {
                Shape.PATH -> canvas.drawPath(pathList[i], paintBrush)
                Shape.LINE -> canvas.drawPath(pathList[i], paintBrush)
                Shape.RECTANGLE -> canvas.drawPath(pathList[i], paintBrush)
                Shape.CIRCLE -> canvas.drawPath(pathList[i], paintBrush)
            }
            //ondraw쪽으로 가서 그림을 다시 그림
            invalidate()
        }

    }

}