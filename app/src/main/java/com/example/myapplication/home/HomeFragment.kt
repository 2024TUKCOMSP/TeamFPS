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
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Paints
import com.example.myapplication.data.Users
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.databinding.RecyclerItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var firebaseRef: DatabaseReference
    lateinit var paintList: MutableList<Paints>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        firebaseRef = FirebaseDatabase.getInstance().getReference("paints")
        paintList = mutableListOf<Paints>()

        fetchData()
        binding.paintingList.layoutManager = LinearLayoutManager(context)
        binding.paintingList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        return binding.root
    }

    private fun fetchData() {
        firebaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                paintList.clear()
                if (snapshot.exists()) {
                    for (paintSnap in snapshot.children) {
                        val paints = paintSnap.getValue(Paints::class.java)
                        paintList.add(paints!!)
                        val adapter = ResultAdapter(paintList)
                        binding.paintingList.adapter = adapter
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "error: $error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

class RecyclerHolder(val binding: RecyclerItemBinding): RecyclerView.ViewHolder(binding.root)

class ResultAdapter(val paintList: MutableList<Paints>):
    RecyclerView.Adapter<RecyclerHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder =
        RecyclerHolder(RecyclerItemBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))

    override fun getItemCount(): Int = paintList.size

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        val currentPaint = paintList[position]
        holder.binding.paintings.setImageDrawable(R.drawable.kakao_login_medium_narrow.toDrawable())
        holder.apply {
            binding.apply {
                textview.text = currentPaint.cost
            }
        }
    }
}