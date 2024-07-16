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
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.databinding.RecyclerItemBinding

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val costs = mutableListOf<String>()
        for (i in 1..45) {
            costs.add("Item $i")
        }

        binding.paintingList.layoutManager = LinearLayoutManager(context)
        val adapter = ResultAdapter(costs)
        binding.paintingList.adapter = adapter
        binding.paintingList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        return binding.root
    }

    class RecyclerHolder(val binding: RecyclerItemBinding): RecyclerView.ViewHolder(binding.root)

    class ResultAdapter(val costs: MutableList<String>):
        RecyclerView.Adapter<RecyclerHolder>(){

//        private val VIEW_TYPE_ITEM = 0
//        private val VIEW_TYPE_LOADING = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder =
            RecyclerHolder(RecyclerItemBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))

        override fun getItemCount(): Int = costs.size

        override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
            holder.binding.paintings.setImageDrawable(R.drawable.kakao_login_medium_narrow.toDrawable())
            holder.binding.textview.text = costs[position]
        }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        var plusbutton = view.findViewById<Button>(R.id.buy_painting)
//        var container = requireContext()
//        plusbutton.setOnClickListener{
//            addLayout() //미완 파베 등록되거나 삭제 되면 완성본이 올라갔으면 좋겠음
//        }
//
//    }

//    private fun addLayout()
//    {
//        var pluslayout = view?.findViewById<LinearLayout>(R.id.drawlayout)
//        val newLayout = LinearLayout(requireContext()).apply {
//            orientation = LinearLayout.HORIZONTAL
//        }
//
//        // 새 레이아웃에 위젯 추가
//        val label = TextView(requireContext()).apply {
//            text = "New Label:"
//        }
//        val editText = EditText(requireContext())
//        val button = Button(requireContext()).apply {
//            text = "New Button"
//        }
//
//        newLayout.addView(label)
//        newLayout.addView(editText)
//        newLayout.addView(button)
//
//        // 기존의 drawlayout에 새 레이아웃 추가
//        pluslayout?.addView(newLayout)
//    }
}