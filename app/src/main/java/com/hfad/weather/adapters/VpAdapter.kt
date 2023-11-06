package com.hfad.weather.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VpAdapter(frag:FragmentActivity,private val list:List<Fragment>):FragmentStateAdapter(frag) {

    override fun getItemCount(): Int {
      return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}