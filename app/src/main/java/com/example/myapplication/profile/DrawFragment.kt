package com.example.myapplication.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.NaviActivity
import com.example.myapplication.data.Paints
import com.example.myapplication.databinding.DrawBuyItemBinding
import com.example.myapplication.databinding.FragmentDrawBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DrawFragment : Fragment() {
    lateinit var binding: FragmentDrawBinding
    lateinit var paintRef: DatabaseReference
    lateinit var paintList: MutableList<Paints>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrawBinding.inflate(inflater, container, false)
        paintRef = FirebaseDatabase.getInstance().getReference("paints")
        paintList = mutableListOf<Paints>()


        //로그인 유저 불러오기
        val pref = requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val uid = pref.getString("login_user", "").toString()
        Log.d("ykyk", "uid in Draw: $uid")

        fetchData(uid)

        binding.paintingList.layoutManager = LinearLayoutManager(context)
        binding.paintingList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        return binding.root
    }

    private fun fetchData(uid: String) {
        paintRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                paintList.clear()
                if (snapshot.exists()) {
                    for (paintSnap in snapshot.children) {
                        val paints = paintSnap.getValue(Paints::class.java)
                        if (paints?.uid == uid)
                            paintList.add(paints)
                    }
                }
                val adapter = DrawBuyAdapter(paintList)
                binding.paintingList.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("ykyk", "error: $error")
            }
        })
    }
}

class DrawBuyHolder(val binding: DrawBuyItemBinding): RecyclerView.ViewHolder(binding.root)

class DrawBuyAdapter(val paintList: MutableList<Paints>):
    RecyclerView.Adapter<DrawBuyHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawBuyHolder =
        DrawBuyHolder(
            DrawBuyItemBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))

    override fun getItemCount(): Int = paintList.size

    override fun onBindViewHolder(holder: DrawBuyHolder, position: Int) {
        val currentPaint = paintList[position]

        holder.apply {
            binding.apply {
                titleText.text = currentPaint.name
                Glide.with(holder.binding.root.context)
                    .load(currentPaint.drawURL)
                    .into(paintings)

            }
        }
    }
}