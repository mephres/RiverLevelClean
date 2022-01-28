package com.intas.metrolog.ui.events.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager)  {
    private val fragmentList = mutableListOf<Fragment>()
    private val fragmentTitleList = mutableListOf<String>()


    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String, position: Int) {
        fragmentList.add(position, fragment)
        fragmentTitleList.add(position, title)
    }

    fun removeFragment(fragment: Fragment?, position: Int) {
        fragmentList.removeAt(position)
        fragmentTitleList.removeAt(position)
    }

    fun removeFragment(position: Int) {
        fragmentList.removeAt(position)
        fragmentTitleList.removeAt(position)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return fragmentTitleList[position]
    }
}