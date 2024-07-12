package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.myapplication.databinding.ActivityNaviBinding
import com.example.myapplication.gallary.GallaryFragment
import com.example.myapplication.home.HomeFragment
import com.example.myapplication.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class NaviActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_navi)
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

    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
    }
}