package com.example.myapplication.profile

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.example.myapplication.Login.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayoutMediator

class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.viewpager.adapter = PagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabs, binding.viewpager){ tab, position ->
            if (position == 0) tab.text = "Draw"
            else tab.text = "Buy"
        }.attach()

        //툴바 세팅
        val toolbar: Toolbar = binding.myPageToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        setHasOptionsMenu(true)

        return binding.root
    }

    //옵션 메뉴를 생성하는 함수
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_profile, menu)
    }

    //메뉴 선택 기능 구현하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_name -> {

                true
            }
            R.id.menu_logout ->{

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}


// 어댑터 클래스
class PagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    //프래그먼트 리스트 선언 및 초기화
    val fragments: List<Fragment>
    init {
        fragments = listOf(DrawFragment(), BuyFragment())
    }
    // 프래그먼트 리스트의 크기 반환
    override fun getItemCount(): Int = fragments.size
    //프래그먼트 생성
    override fun createFragment(position: Int): Fragment = fragments[position]
}