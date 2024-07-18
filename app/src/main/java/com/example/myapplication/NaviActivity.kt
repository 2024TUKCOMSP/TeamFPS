package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.myapplication.databinding.ActivityNaviBinding
import com.example.myapplication.databinding.CustomDialogBinding
import com.example.myapplication.gallary.GallaryFragment
import com.example.myapplication.home.HomeFragment
import com.example.myapplication.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase

class NaviActivity : AppCompatActivity() {

    lateinit var binding : ActivityNaviBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bottomNavigationView =findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val profileFragment = ProfileFragment()
        val homeFragment = HomeFragment()
        val gallaryFragment = GallaryFragment()
        replaceFragment(homeFragment)
        bottomNavigationView.selectedItemId = R.id.home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.profile -> replaceFragment(profileFragment)
                R.id.home -> replaceFragment(homeFragment)
                R.id.draw-> replaceFragment(gallaryFragment)
            }
            true
        }
        //로그인 토큰 받기
        val token = intent.getStringExtra("TOKEN")
    }



    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
    }
}