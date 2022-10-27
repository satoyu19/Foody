package com.example.foody.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(private val resultBundle: Bundle,
                   private var fragments: ArrayList<Fragment>,
                   fragmentActivity: FragmentActivity
                    ): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = fragments.size

    //インスタンスを再利用するのではなく、関数が呼び出されるたびに常に新しいフラグメント インスタンスを提供
    override fun createFragment(position: Int): Fragment {
        fragments[position].arguments = resultBundle
        return fragments[position]
    }

}