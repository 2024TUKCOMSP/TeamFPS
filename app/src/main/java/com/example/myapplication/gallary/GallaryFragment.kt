package com.example.myapplication.gallary

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

        redBtn.setOnClickListener{
            paintBrush.color = Color.RED
            currentColor(paintBrush.color)
            for(i in xpathList.indices)
            Log.d("path", xpathList[i].toString())

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
            for (i in pathList.indices){
                var pathlog = Log.d("pathlist",pathList[i].toString())
            }
            pathList.clear()
            colorList.clear()
            xpathList.clear()
            ypathList.clear()
            path.reset()
        }
        //그림 저장하는 데베 코드
        paintend.setOnClickListener{

            val customDialog = SelectDrawingCostName(requireContext())
            customDialog.show()
            val token = (requireActivity() as? NaviActivity)?.getToken()
            val uid = token.hashCode().toString()


        }

    }
    private fun currentColor(color: Int){
        currentBrush = color
        path = Path()
    }

}