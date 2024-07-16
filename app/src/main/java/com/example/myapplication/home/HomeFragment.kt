package com.example.myapplication.home

import android.os.Bundle
import android.text.Layout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.myapplication.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var plusbutton = view.findViewById<Button>(R.id.buy_painting)
        var container = requireContext()
        plusbutton.setOnClickListener{
            addLayout() //미완 파베 등록되거나 삭제 되면 완성본이 올라갔으면 좋겠음
        }

    }
    private fun addLayout()
    {
        var pluslayout = view?.findViewById<LinearLayout>(R.id.drawlayout)
        val newLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        // 새 레이아웃에 위젯 추가
        val label = TextView(requireContext()).apply {
            text = "New Label:"
        }
        val editText = EditText(requireContext())
        val button = Button(requireContext()).apply {
            text = "New Button"
        }

        newLayout.addView(label)
        newLayout.addView(editText)
        newLayout.addView(button)

        // 기존의 drawlayout에 새 레이아웃 추가
        pluslayout?.addView(newLayout)
    }


}