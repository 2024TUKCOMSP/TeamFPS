package com.example.myapplication.gallary

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.myapplication.NaviActivity
import com.example.myapplication.R
import com.example.myapplication.data.Paints
import com.example.myapplication.data.Pid
import com.example.myapplication.gallary.PaintView.Companion.colorList
import com.example.myapplication.gallary.PaintView.Companion.currentBrush
import com.example.myapplication.gallary.PaintView.Companion.pathList
import com.example.myapplication.gallary.PaintView.Companion.xpathList
import com.example.myapplication.gallary.PaintView.Companion.ypathList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class GallaryFragment : Fragment() {

    companion object{
        var path = Path()
        var paintBrush = Paint()
    }



    // TODO: Rename and change types of parameters
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallary, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val redBtn = view.findViewById<ImageButton>(R.id.redColor)
        val blueBtn = view.findViewById<ImageButton>(R.id.blueColor)
        val blackBtn = view.findViewById<ImageButton>(R.id.blackColor)
        val eraser = view.findViewById<ImageButton>(R.id.whiteColor)
        val paintend = view.findViewById<Button>(R.id.paintendbutton)
        val seepaint = view.findViewById<Button>(R.id.seedrawing)

        redBtn.setOnClickListener{
            paintBrush.color = Color.RED
            currentColor(paintBrush.color)
        }
        blueBtn.setOnClickListener{
            paintBrush.color = Color.BLUE
            currentColor(paintBrush.color)
        }
        blackBtn.setOnClickListener{
            paintBrush.color = Color.BLACK
            currentColor(paintBrush.color)
        }
        eraser.setOnClickListener{
            pathList.clear()
            colorList.clear()
            xpathList.clear()
            ypathList.clear()
            path.reset()
        }
        //그림 보는 코드
        seepaint.setOnClickListener{
            var intnet: Intent = Intent(requireContext(),SeeDrawActivity::class.java)
            startActivity(intnet)
        }

        //그림 저장하는 데베 코드
        paintend.setOnClickListener {

            val customDialog = SelectDrawingCostName(requireContext())
            customDialog.show()



            //토큰
            val token = (requireActivity() as? NaviActivity)?.getToken()
            val uid = token.hashCode().toString()


            val database = FirebaseDatabase.getInstance()
            //그림 데베 정보 접근
            val paintsRef = database.getReference("paints")
            //pid 데베 정보 접근
            val pidRef = database.getReference("pid")
            pidRef.child("1").addListenerForSingleValueEvent(object : ValueEventListener {
                //데이터를 성공적으로 읽어본 경우
                override fun onDataChange(snapshot: DataSnapshot) {
                    val retrievedPid = snapshot.getValue(Pid::class.java)
                    //Log.d("yang","token2")
                    if (snapshot.exists()) {
                        var Intpid = retrievedPid?.pid?.plus(1)
                        val setpid = Pid(Intpid)
                        pidRef.child("1").setValue(setpid)

                        val paint = Paints(
                            Intpid.toString(),
                            uid,
                            "",
                            "",
                            0,
                            xpathList,
                            ypathList,
                            colorList,
                            ""
                        )
                        paintsRef.child(paint.pid!!).setValue(paint)
                    }
                    pathList.clear()
                    colorList.clear()
                    xpathList.clear()
                    ypathList.clear()
                    path.reset()
                }

                //데이터 읽기가 실패한 경우
                override fun onCancelled(e: DatabaseError) {
                    Log.d("yang", "데이터 호출 실패: $e")
                }
            })
        }
        }
    private fun currentColor(color: Int){
        currentBrush = color
        path = Path()
    }



}