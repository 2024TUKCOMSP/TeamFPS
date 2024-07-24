package com.example.myapplication.home

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
import com.google.firebase.database.getValue
import com.kakao.sdk.cert.a.a
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf.Visibility

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var paintRef: DatabaseReference
    lateinit var userRef: DatabaseReference
    lateinit var paintList: MutableList<Paints>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        paintRef = FirebaseDatabase.getInstance().getReference("paints")
        userRef = FirebaseDatabase.getInstance().getReference("users")
        paintList = mutableListOf<Paints>()


        val uid = arguments?.getString("UID")

        if (uid != null) {
            fetchUser(uid)
            fetchData(uid)
        }
        
        binding.paintingList.layoutManager = LinearLayoutManager(context)
        binding.paintingList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        return binding.root
    }

    private fun fetchUser(uid: String) {
        //데이터 변경 인식
        userRef.child(uid).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(Users::class.java)
                    binding.nameText.text = user?.nickname
                    binding.moneyText.text = user?.money
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("ykyk", "error: $error")
            }
        })
    }

    private fun fetchData(uid: String) {
        paintRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                paintList.clear()
                if (snapshot.exists()) {
                    for (paintSnap in snapshot.children) {
                        val paints = paintSnap.getValue(Paints::class.java)
                        Log.d("ykyk", "paint $paints")
                        if (paints?.sell == 1||paints?.uid == uid)
                            continue
                        paintList.add(paints!!)
                    }
                }
                val adapter = ResultAdapter(paintList, uid)
                binding.paintingList.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("ykyk", "error: $error")
            }
        })
    }
}

class RecyclerHolder(val binding: RecyclerItemBinding): RecyclerView.ViewHolder(binding.root){
}

class ResultAdapter(val paintList: MutableList<Paints>, val uid: String):
    RecyclerView.Adapter<RecyclerHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder =
        RecyclerHolder(RecyclerItemBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))

    override fun getItemCount(): Int = paintList.size

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        val currentPaint = paintList[position]
        val userRef = FirebaseDatabase.getInstance().getReference("users")
        val paintRef = FirebaseDatabase.getInstance().getReference("paints")
        Log.d("ykyk", "uid in re $uid")


        holder.binding.buyBtn.setOnClickListener {
            Log.d("ykyk", "버튼 눌림")
            userRef.child(uid).addListenerForSingleValueEvent(object: ValueEventListener {
                //데이터를 성공적으로 읽어본 경우
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ykyk", "snapshot $snapshot")
                    val currentUser = snapshot.getValue(Users::class.java)
                    if (currentUser != null) {
                        Log.d("ykyk", "null 검사")
                        if (currentPaint.cost!!.toInt() > currentUser.money!!.toInt()) {
                            Log.d("ykyk", "돈 부족")
                            Toast.makeText(holder.binding.root.context, "마일리지가 부족합니다.", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Log.d("ykyk", "돈 충분")
                            val change = currentUser.money!!.toInt() - currentPaint.cost!!.toInt()
                            userRef.child(currentUser.uid!!).child("money").setValue(change.toString())
                            Log.d("ykyk", "change: $change")
                            Toast.makeText(holder.binding.root.context, "구매하였습니다.", Toast.LENGTH_SHORT).show()
                            paintRef.child(currentPaint.pid!!).child("sell").setValue(1)
                            getMoney(currentPaint.uid!!, currentPaint.cost!!)
                            paintRef.child(currentPaint.pid!!).child("owner").setValue(uid)
                        }
                    }
                    else
                        Log.d("ykyk", "currentUser : null")
                    Log.d("ykyk", "currentUser $currentUser")
                }
                //데이터 읽기가 실패한 경우
                override fun onCancelled(error: DatabaseError) {
                    Log.d("ykyk", "error: $error")
                }

                private fun getMoney(uid: String, cost: String) {
                    userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(Users::class.java)
                            if (user != null) {
                                val money = (user.money!!.toInt() + cost.toInt()).toString()
                                userRef.child(uid).child("money").setValue(money)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("ykyk", "error: $error")
                        }
                    })
                }
            })
        }

        holder.apply {
            binding.apply {
                textview.text = currentPaint.cost
                titleText.text = currentPaint.name
                Glide.with(holder.binding.root.context)
                    .load(currentPaint.drawURL)
                    .into(paintings)
            }
        }
    }
}