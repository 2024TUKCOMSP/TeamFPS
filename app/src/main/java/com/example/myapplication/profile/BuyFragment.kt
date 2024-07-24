package com.example.myapplication.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.NaviActivity
import com.example.myapplication.data.Paints
import com.example.myapplication.databinding.FragmentBuyBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BuyFragment : Fragment() {
    lateinit var binding: FragmentBuyBinding
    lateinit var paintRef: DatabaseReference
    lateinit var paintList: MutableList<Paints>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyBinding.inflate(inflater, container, false)
        paintRef = FirebaseDatabase.getInstance().getReference("paints")
        paintList = mutableListOf<Paints>()

        val token = (requireActivity() as? NaviActivity)?.getToken()
        val uid = token.hashCode().toString()
        Log.d("ykyk", "uid: $uid")

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
                        if (paints?.owner == uid)
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