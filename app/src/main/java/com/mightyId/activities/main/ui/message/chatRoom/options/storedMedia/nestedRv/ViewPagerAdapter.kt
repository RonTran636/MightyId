package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.nestedRv

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(val context: FragmentActivity,val topicId:String) : FragmentStateAdapter(context){
    private val listFragment = mutableMapOf<Int,Fragment>()
    override fun createFragment(position: Int): Fragment {
        val fragment = ViewPagerHolder.newInstance(position, topicId)
        listFragment[position] = fragment
        return fragment
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun onTodoStatusChange(todoId: Int, status: String){
        val fragment = listFragment[0] as ViewPagerHolder
        fragment.updateTodoList(todoId,status)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = 4
}